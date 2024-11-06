package com.jindi.infra.traffic.sentinel.interceptor;

import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.traffic.sentinel.constant.SentinelConsts;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.Map;

@Slf4j
@Order(-1000)
public class HeaderCallInterceptor implements CallInterceptor {

	private String applicationName;

	public HeaderCallInterceptor(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
		extHeaders.put(SentinelConsts.RPC_ORIGIN, applicationName);
	}
}
