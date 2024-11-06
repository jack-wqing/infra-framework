package com.jindi.infra.governance.lane.grpc;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.core.constants.LaneTagThreadLocal;
import com.jindi.infra.grpc.extension.CallInterceptor;

import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LaneCallInterceptor implements CallInterceptor {

	@Override
	public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
		try {
			String value = LaneTagThreadLocal.getLaneTag();
			if (StringUtils.isNotBlank(value)) {
				extHeaders.put(LaneTagThreadLocal.LANE_TAG_KEY, value);
			}
		} catch (Throwable e) {
			log.error("", e);
		}
	}
}
