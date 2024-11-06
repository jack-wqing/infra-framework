package com.jindi.infra.traffic.sentinel.interceptor;

import com.alibaba.csp.sentinel.context.ContextUtil;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE + 2000)
public class ContextEnterClientInterceptor implements ClientInterceptor {

	private static final String DEFAULT = "default";
	private String applicationName;

	public ContextEnterClientInterceptor(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
			CallOptions callOptions, Channel channel) {
		ContextUtil.enter(DEFAULT, applicationName);
		return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
				channel.newCall(methodDescriptor, callOptions)) {
		};
	}
}
