package com.jindi.infra.grpc.extension;

import java.util.Map;

import io.grpc.MethodDescriptor;

public interface CallInterceptor {
	/**
	 * 请求开始
	 *
	 * @param id
	 *            请求ID
	 * @param method
	 *            方法描述信息
	 * @param extHeaders
	 *            请求头
	 */
	default void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {

	}

	/**
	 * 请求结束
	 *
	 * @param id
	 *            请求ID
	 * @param method
	 *            方法描述信息
	 * @param cause
	 *            异常
	 */
	default void after(Long id, MethodDescriptor method, Throwable cause) {
	}
}
