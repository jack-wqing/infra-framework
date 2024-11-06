package com.jindi.infra.traffic.sentinel.mvc.parser;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;

import javax.servlet.http.HttpServletRequest;

import static com.jindi.infra.traffic.sentinel.constant.SentinelConsts.HEADER_APP_NAME;


public class SentinelHeaderOriginParser implements RequestOriginParser{
    @Override
    public String parseOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HEADER_APP_NAME);
        if (StringUtils.isNotBlank(origin)) {
            return origin;
        }
        return "";
    }

}
