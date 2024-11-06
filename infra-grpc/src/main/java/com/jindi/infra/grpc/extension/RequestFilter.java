package com.jindi.infra.grpc.extension;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * 请求过滤器
 *
 * @param <ReqT>
 * @param <RespT>
 */
public interface RequestFilter<ReqT, RespT> {

	/**
	 * 过滤器；在方法执行前调用
	 *
	 * @param method
	 *            方法描述信息
	 * @param headers
	 *            请求头
	 */
	void before(Long id, MethodDescriptor<ReqT, RespT> method, Metadata headers);

	/**
	 * 过滤器；在方法执行后调用
	 *
	 * @param method
	 *            方法描述信息
	 * @param cause
	 *            异常
	 */
	default void after(Long id, MethodDescriptor<ReqT, RespT> method, Throwable cause) {
	}
}
