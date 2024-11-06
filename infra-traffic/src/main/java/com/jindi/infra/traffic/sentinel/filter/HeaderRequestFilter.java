package com.jindi.infra.traffic.sentinel.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;

import com.alibaba.csp.sentinel.context.ContextUtil;
import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.traffic.sentinel.constant.SentinelConsts;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

@Order(-1000)
public class HeaderRequestFilter implements RequestFilter {

	private static final String DEFAULT = "default";

	@Override
	public void before(Long id, MethodDescriptor method, Metadata headers) {
		String rpcOriginValue = GrpcHeaderUtils.getHeaderValue(SentinelConsts.RPC_ORIGIN, headers);
		if (StringUtils.isNotBlank(rpcOriginValue)) {
			ContextUtil.enter(DEFAULT, rpcOriginValue);
		}
	}

	@Override
	public void after(Long id, MethodDescriptor method, Throwable cause) {
		ContextUtil.exit();
	}
}
