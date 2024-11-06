package com.jindi.infra.traffic.sentinel.interceptor;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.core.annotation.Order;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.jindi.infra.grpc.constant.ProtoGoogleJavaMapping;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * gRPC server interceptor for Sentinel. Currently it only works with unary
 * methods.
 *
 * <p>
 * Example code:
 *
 * <pre>
 * Server server = ServerBuilder.forPort(port).addService(new MyServiceImpl()) // Add your service.
 * 		.intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.
 * 		.build();
 * </pre>
 *
 * <p>
 * For client interceptor, see
 * {@link com.jindi.infra.traffic.sentinel.interceptor.SentinelGrpcClientInterceptor}.
 *
 * @author Eric Zhao
 */
@Slf4j
@Order(2000)
public class SentinelGrpcServerInterceptor implements ServerInterceptor {
	private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE
			.withDescription("Flow control limit exceeded (server side)");
	private static final StatusRuntimeException STATUS_RUNTIME_EXCEPTION = new StatusRuntimeException(Status.CANCELLED);

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		String fullMethodName = call.getMethodDescriptor().getFullMethodName();
		// 将google包名转换成java包名
		fullMethodName = ProtoGoogleJavaMapping.convert(fullMethodName);
		Entry entry = null;
		try {
			entry = SphU.asyncEntry(fullMethodName, EntryType.IN);
			final AtomicReference<Entry> atomicReferenceEntry = new AtomicReference<>(entry);
			// Allow access, forward the call.
			return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
					next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
						@Override
						public void close(Status status, Metadata trailers) {
							Entry entry = atomicReferenceEntry.get();
							if (entry != null) {
								// Record the exception metrics.
								if (!status.isOk()) {
									Tracer.traceEntry(status.asRuntimeException(), entry);
								}
								// entry exit when the call be closed
								entry.exit();
							}
							super.close(status, trailers);
						}
					}, headers)) {
				/**
				 * If call was canceled, onCancel will be called. and the close will not be
				 * called so the server is encouraged to abort processing to save resources by
				 * onCancel
				 *
				 * @see ServerCall.Listener#onCancel()
				 */
				@Override
				public void onCancel() {
					Entry entry = atomicReferenceEntry.get();
					if (entry != null) {
						Tracer.traceEntry(STATUS_RUNTIME_EXCEPTION, entry);
						entry.exit();
						atomicReferenceEntry.set(null);
					}
					super.onCancel();
				}
			};
		} catch (BlockException e) {
			call.close(FLOW_CONTROL_BLOCK, new Metadata());
			return new ServerCall.Listener<ReqT>() {
			};
		} catch (RuntimeException e) {
			// Catch the RuntimeException startCall throws, entry is guaranteed to exit.
			if (entry != null) {
				Tracer.traceEntry(e, entry);
				entry.exit();
			}
			throw e;
		}
	}

}
