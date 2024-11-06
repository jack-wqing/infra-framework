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

/**
 * 设置EndInterceptor解决跨线程无法清空ContextUtil
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class ContextClearClientInterceptor implements ClientInterceptor {

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
			CallOptions callOptions, Channel channel) {
		ContextUtil.exit();
		return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
				channel.newCall(methodDescriptor, callOptions)) {
		};
	}
}
