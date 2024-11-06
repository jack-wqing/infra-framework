package com.jindi.infra.feign.interceptor;

import com.jindi.infra.feign.constant.FeignConsts;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignCallInterceptor implements RequestInterceptor {

    private static final String COOKIE = "Cookie";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(COOKIE, FeignConsts.ORIGIN_COOKIE);
    }
}
