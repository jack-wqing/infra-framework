package com.jindi.infra.grpc.extension;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public interface RequestFilter<ReqT, RespT> {

	void before(Long id, MethodDescriptor<ReqT, RespT> method, Metadata headers);

	default void after(Long id, MethodDescriptor<ReqT, RespT> method, Throwable cause) {
	}
}
