package com.jindi.infra.governance.lane;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.core.constants.LaneTagThreadLocal;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class LaneOpenFeignRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		String lane = LaneTagThreadLocal.getLaneTag();
		if (StringUtils.isNotBlank(lane)) {
			requestTemplate.header(LaneTagThreadLocal.LANE_TAG_KEY, lane);
		}
	}
}
