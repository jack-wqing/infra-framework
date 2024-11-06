package com.jindi.infra.feign.interceptor;

import static com.jindi.infra.feign.constant.FeignConsts.ORIGIN;
import static com.jindi.infra.feign.constant.FeignConsts.TRUE;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignCallInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		requestTemplate.header(ORIGIN, TRUE);
	}
}
