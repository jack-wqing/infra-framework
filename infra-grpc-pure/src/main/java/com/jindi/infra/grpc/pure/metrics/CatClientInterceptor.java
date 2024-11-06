package com.jindi.infra.grpc.pure.metrics;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatClientInterceptor implements ClientInterceptor {

	private static final String RPC_CLIENT = "Rpc.Client";

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		String name = getMethodName(RPC_CLIENT, method);
		Cat.logMetricForCount(name, 1);
		final AtomicBoolean run = new AtomicBoolean(false);
		long startTime = System.currentTimeMillis();
		return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				super.start(
						new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
							@Override
							public void onClose(Status status, Metadata trailers) {
								if (run.compareAndSet(false, true)) {
									Transaction transaction = Cat.newTransaction(RPC_CLIENT, name);
									try {
										if (status.isOk()) {
											transaction.setStatus(Message.SUCCESS);
										} else {
											transaction.setStatus("ERROR");
										}
									} catch (Throwable e) {
										log.error("cat", e);
									} finally {
										transaction.setDurationInMillis(System.currentTimeMillis() - startTime);
										transaction.complete();
									}
								}
								super.onClose(status, trailers);
							}
						}, headers);
			}

			@Override
			public void cancel(String message, Throwable cause) {
				if (run.compareAndSet(false, true)) {
					Transaction transaction = Cat.newTransaction(RPC_CLIENT, name);
					try {
						transaction.setStatus("ERROR");
					} catch (Throwable e) {
						log.error("cat", e);
					} finally {
						transaction.setDurationInMillis(System.currentTimeMillis() - startTime);
						transaction.complete();
					}
				}
				super.cancel(message, cause);
			}
		};
	}

	private <ReqT, RespT> String getMethodName(String title, MethodDescriptor<ReqT, RespT> methodDescriptor) {
		return String.format("%s:%s()", title, methodDescriptor.getFullMethodName());
	}
}
