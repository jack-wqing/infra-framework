package com.jindi.infra.feign.interceptor;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import com.jindi.infra.core.constants.HeaderConsts;
import com.jindi.infra.feign.constant.FeignConsts;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeignCallMultiProtocolInterceptor implements RequestInterceptor {

    private final String PARAM_SPLIT = "?";

    private final List<MultiProtocolClientInterceptor> interceptorList;
    public FeignCallMultiProtocolInterceptor(List<MultiProtocolClientInterceptor> multiProtocolClientInterceptorList) {
        this.interceptorList = multiProtocolClientInterceptorList;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> extHeaders = new HashMap<>();
        String path = getUrl(requestTemplate);
        for (MultiProtocolClientInterceptor clientInterceptor : interceptorList) {
            clientInterceptor.before(path, extHeaders);
        }
        if (MapUtil.isNotEmpty(extHeaders)) {
            requestTemplate.header(HeaderConsts.INFRA_CONTEXT_HEADER_KEY, InnerJSONUtils.toJSONString(extHeaders));
        }
    }

    private String getUrl(RequestTemplate requestTemplate) {
        String url = requestTemplate.url();
        url = StrUtil.subBefore(url, PARAM_SPLIT, false);
        return requestTemplate.feignTarget() != null ? requestTemplate.feignTarget().url() + url : url;
    }
}
