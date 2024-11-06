package com.jindi.infra.grpc.server;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.jindi.infra.grpc.extension.RequestFilter;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 简单服务端拦截器，为了降低grpc拦截器使用成本，抽象一些拦截器操作给业务使用
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
public class SimpleServerInterceptor implements ServerInterceptor {

	private static final StatusRuntimeException STATUS_RUNTIME_EXCEPTION = new StatusRuntimeException(Status.CANCELLED);
	private static final AtomicLong ID = new AtomicLong(0);
	private final AtomicReference<List<RequestFilter>> requestFiltersReference = new AtomicReference();

	public void setRequestFiltersReference(List<RequestFilter> requestFilters) {
		requestFiltersReference.set(requestFilters);
	}

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		List<RequestFilter> requestFilters = null;
		if (requestFiltersReference.get() != null && !requestFiltersReference.get().isEmpty()) {
			requestFilters = requestFiltersReference.get();
		}
		long id = ID.incrementAndGet();
		if (requestFilters != null) {
			for (RequestFilter requestFilter : requestFilters) {
				if (requestFilter == null) {
					continue;
				}
				requestFilter.before(id, call.getMethodDescriptor(), headers);
			}
		}

		final AtomicBoolean run = new AtomicBoolean(false);
		List<RequestFilter> finalRequestFilters = requestFilters;
		return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
				next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
					@Override
					public void close(Status status, Metadata trailers) {
						if (run.compareAndSet(false, true)) {
							if (finalRequestFilters != null) {
								for (RequestFilter requestFilter : finalRequestFilters) {
									if (requestFilter == null) {
										continue;
									}
									if (status.isOk()) {
										requestFilter.after(id, call.getMethodDescriptor(), null);
									} else {
										requestFilter.after(id, call.getMethodDescriptor(),
												status.asRuntimeException());
									}
								}
							}
						}
						super.close(status, trailers);
					}
				}, headers)) {

			@Override
			public void onCancel() {
				if (run.compareAndSet(false, true)) {
					if (finalRequestFilters != null) {
						for (RequestFilter requestFilter : finalRequestFilters) {
							if (requestFilter == null) {
								continue;
							}
							requestFilter.after(id, call.getMethodDescriptor(), STATUS_RUNTIME_EXCEPTION);
						}
					}
				}
				super.onCancel();
			}
		};
	}
}
