package com.jindi.infra.grpc.pure.metrics;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatServerInterceptor implements ServerInterceptor {

	private static final String RPC_SERVER = "Rpc.Server";

	private static final StatusRuntimeException STATUS_RUNTIME_EXCEPTION = new StatusRuntimeException(Status.CANCELLED);

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		String name = getMethodName(RPC_SERVER, call.getMethodDescriptor());
		Cat.logMetricForCount(name, 1);
		final AtomicBoolean run = new AtomicBoolean(false);
		long startTime = System.currentTimeMillis();
		return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
				next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
					@Override
					public void close(Status status, Metadata trailers) {
						if (run.compareAndSet(false, true)) {
							Transaction transaction = Cat.newTransaction(RPC_SERVER, name);
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
						super.close(status, trailers);
					}
				}, headers)) {

			@Override
			public void onCancel() {
				if (run.compareAndSet(false, true)) {
					Transaction transaction = Cat.newTransaction(RPC_SERVER, name);
					try {
						transaction.setStatus("ERROR");
					} catch (Throwable e) {
						log.error("cat", e);
					} finally {
						transaction.setDurationInMillis(System.currentTimeMillis() - startTime);
						transaction.complete();
					}
				}
				super.onCancel();
			}
		};
	}

	private <ReqT, RespT> String getMethodName(String title, MethodDescriptor<ReqT, RespT> methodDescriptor) {
		return String.format("%s:%s()", title, methodDescriptor.getFullMethodName());
	}
}
