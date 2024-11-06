package com.jindi.infra.grpc.channel;

import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;

public class NullManagedChannel extends ManagedChannel {

	@Override
	public ManagedChannel shutdown() {
		return null;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public ManagedChannel shutdownNow() {
		return null;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
			MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
		return null;
	}

	@Override
	public String authority() {
		return null;
	}
}
