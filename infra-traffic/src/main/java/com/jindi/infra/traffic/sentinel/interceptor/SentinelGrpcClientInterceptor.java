package com.jindi.infra.traffic.sentinel.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.jindi.infra.core.constants.EventType;
import com.jindi.infra.core.constants.GrpcConsts;
import com.jindi.infra.core.constants.MethodType;
import com.jindi.infra.core.exception.RpcBlockException;
import com.jindi.infra.core.model.RpcInvokeEvent;
import com.jindi.infra.grpc.constant.ProtoGoogleJavaMapping;
import com.jindi.infra.grpc.util.GrpcUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * gRPC client interceptor for Sentinel. Currently it only works with unary
 * methods.
 *
 * <p>
 * Example code:
 *
 * <pre>
 * public class ServiceClient {
 *
 * 	private final ManagedChannel channel;
 *
 * 	ServiceClient(String host, int port) {
 * 		this.channel = ManagedChannelBuilder.forAddress(host, port).intercept(new SentinelGrpcClientInterceptor()) // Add
 * 																													// the
 * 																													// client
 * 																													// interceptor.
 * 				.build();
 * 		// Init your stub here.
 * 	}
 *
 * }
 * </pre>
 *
 * <p>
 * For server interceptor, see {@link SentinelGrpcServerInterceptor}.
 *
 * @author Eric Zhao
 */
@Slf4j
@Order(2000)
public class SentinelGrpcClientInterceptor implements ClientInterceptor {

	private static final char SEPARATOR = '/';

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
			CallOptions callOptions, Channel channel) {
		String fullMethodName = methodDescriptor.getFullMethodName();
		// 将google包名转换成java包名
		fullMethodName = ProtoGoogleJavaMapping.convert(fullMethodName);

		Entry entry = null;
		try {
			entry = SphU.asyncEntry(fullMethodName, EntryType.OUT);
			final AtomicReference<Entry> atomicReferenceEntry = new AtomicReference<>(entry);
			// Allow access, forward the call.
			return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
					channel.newCall(methodDescriptor, callOptions)) {
				@Override
				public void start(Listener<RespT> responseListener, Metadata headers) {
					super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
							responseListener) {
						@Override
						public void onClose(Status status, Metadata trailers) {
							Entry entry = atomicReferenceEntry.get();
							if (entry != null) {
								// Record the exception metrics.
								if (!status.isOk()) {
									StatusRuntimeException statusRuntimeException = status.asRuntimeException(trailers);
									// 尝试将grpc异常转为原生异常
									Throwable throwable = GrpcUtils.parseStatusRuntimeException(statusRuntimeException);
									Tracer.traceEntry(throwable, entry);
								}
								entry.exit();
								atomicReferenceEntry.set(null);
							}
							super.onClose(status, trailers);
						}
					}, headers);
				}

				/** Some Exceptions will only call cancel. */
				@Override
				public void cancel(@Nullable String message, @Nullable Throwable cause) {
					Entry entry = atomicReferenceEntry.get();
					// Some Exceptions will call onClose and cancel.
					if (entry != null) {
						// Record the exception metrics.
						Tracer.traceEntry(cause, entry);
						entry.exit();
						atomicReferenceEntry.set(null);
					}
					super.cancel(message, cause);
				}
			};
		} catch (BlockException e) {
			StringBuilder message = new StringBuilder("");
			// Flow control threshold exceeded, block the call.
			AbstractRule rule = e.getRule();
			if (rule instanceof FlowRule) {
				FlowRule flowRule = (FlowRule) rule;
				log.error("调用方法{}触发主调限流", flowRule.getResource());
				publishRpcInvokeEvent(methodDescriptor, EventType.RPC_CLIENT_SENTINEL_FLOW);
				message.append(String.format("调用方法{%s}触发主调限流", rule.getResource()));
			} else if (rule instanceof DegradeRule) {
				DegradeRule degradeRule = (DegradeRule) rule;
				log.error("调用方法{}触发主调熔断", degradeRule.getResource());
				publishRpcInvokeEvent(methodDescriptor, EventType.RPC_CLIENT_SENTINEL_DEGRADE);
				message.append(String.format("调用方法{%s}触发主调熔断", rule.getResource()));
			}
			return new ClientCall<ReqT, RespT>() {
				@Override
				public void start(Listener<RespT> responseListener, Metadata headers) {
					Metadata metadata = new Metadata();
					RpcBlockException rpcBlockException = new RpcBlockException(message.toString(), e);
					metadata.put(GrpcConsts.EXCEPTION_STACK_TRACE_KEY,
							com.jindi.infra.core.util.GrpcUtils.getStackTrace(rpcBlockException));
					responseListener.onClose(Status.UNKNOWN, metadata);
				}

				@Override
				public void request(int numMessages) {
				}

				@Override
				public void cancel(@Nullable String message, @Nullable Throwable cause) {
				}

				@Override
				public void halfClose() {
				}

				@Override
				public void sendMessage(ReqT message) {
				}
			};
		} catch (RuntimeException e) {
			// Catch the RuntimeException newCall throws, entry is guaranteed to exit.
			if (entry != null) {
				Tracer.traceEntry(e, entry);
				entry.exit();
			}
			throw e;
		}
	}

	private void publishRpcInvokeEvent(MethodDescriptor methodDescriptor, EventType eventType) {
		String fullMethodName = methodDescriptor.getFullMethodName();
		String method = StringUtils.substringAfterLast(fullMethodName, SEPARATOR);
		applicationEventPublisher.publishEvent(
				new RpcInvokeEvent(this, methodDescriptor.getServiceName(), method, MethodType.UNARY, eventType));
	}
}
