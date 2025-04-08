package com.jindi.infra.grpc.extension;

import java.util.Map;

import io.grpc.MethodDescriptor;

public interface CallInterceptor {

	default void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {

	}

	default void after(Long id, MethodDescriptor method, Throwable cause) {

	}
}
