package com.jindi.infra.grpc.client;

import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.model.CallContext;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

//简单拦截抽象场景
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
public class SimpleClientInterceptor implements ClientInterceptor {

	private static final AtomicLong ID = new AtomicLong(0);
	private final AtomicReference<List<CallInterceptor>> callInterceptorsReference = new AtomicReference();

	public void setCallInterceptorsReference(List<CallInterceptor> callInterceptors) {
		callInterceptorsReference.set(callInterceptors);
	}

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		List<CallInterceptor> callInterceptors = null;
		if (callInterceptorsReference.get() != null && !callInterceptorsReference.get().isEmpty()) {
			callInterceptors = callInterceptorsReference.get();
		}
		List<CallInterceptor> finalCallInterceptors = callInterceptors;
		final AtomicBoolean run = new AtomicBoolean(false);
		long id = ID.incrementAndGet();
		CallContext callContext = CallContext.currentCallContext();
		return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				try {
					Map<String, String> extHeaders = new HashMap<>();
					if (finalCallInterceptors != null) {
						for (CallInterceptor callInterceptor : finalCallInterceptors) {
							if (callInterceptor == null) {
								continue;
							}
							callInterceptor.before(id, method, extHeaders);
						}
					}
					if (StringUtils.isNotBlank(RpcConsts.GATEWAY_ROUTING_VALUE.get())) {
						extHeaders.put(RpcConsts.GATEWAY_ROUTING_KEY, RpcConsts.GATEWAY_ROUTING_VALUE.get());
					} else {
						extHeaders.put(RpcConsts.GATEWAY_ROUTING_KEY, callContext.getServerName());
					}
					extHeaders.put(HttpHeaders.CONTENT_TYPE, "application/grpc");
					if (!CollectionUtils.isEmpty(extHeaders)) {
						for (Map.Entry<String, String> entry : extHeaders.entrySet()) {
							Metadata.Key<String> metadataKey = GrpcHeaderUtils.getMetadataKeyOrCreate(entry.getKey());
							if (metadataKey == null) {
								continue;
							}
							headers.put(metadataKey, entry.getValue());
						}
					}
				} catch (Throwable e) {
					log.error("", e);
				}
				super.start(
						new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
							@Override
							public void onHeaders(Metadata headers) {
								super.onHeaders(headers);
							}

							@Override
							public void onClose(Status status, Metadata trailers) {
								if (run.compareAndSet(false, true)) {
									try {
										if (finalCallInterceptors != null) {
											for (CallInterceptor callInterceptor : finalCallInterceptors) {
												if (callInterceptor == null) {
													continue;
												}
												if (status.isOk()) {
													callInterceptor.after(id, method, null);
												} else {
													callInterceptor.after(id, method, status.asRuntimeException());
												}
											}
										}
									} catch (Throwable e) {
										log.error("", e);
									}
								}
								super.onClose(status, trailers);
							}
						}, headers);
			}

			@Override
			public void cancel(String message, Throwable cause) {
				if (run.compareAndSet(false, true)) {
					try {
						if (finalCallInterceptors != null) {
							for (CallInterceptor callInterceptor : finalCallInterceptors) {
								if (callInterceptor == null) {
									continue;
								}
								callInterceptor.after(id, method, cause);
							}
						}
					} catch (Throwable e) {
						log.error("", e);
					}
				}
				super.cancel(message, cause);
			}
		};
	}
}
