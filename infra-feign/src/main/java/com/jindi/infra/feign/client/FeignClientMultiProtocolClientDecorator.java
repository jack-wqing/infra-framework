package com.jindi.infra.feign.client;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeignClientMultiProtocolClientDecorator implements Client {

    private final String PARAM_SPLIT = "?";

    private final Client delegate;

    private final List<MultiProtocolClientInterceptor> interceptorList;

    public FeignClientMultiProtocolClientDecorator(Client delegate, ObjectProvider<List<MultiProtocolClientInterceptor>> interceptorList) {
        this.delegate = delegate;
        List<MultiProtocolClientInterceptor> interceptors = interceptorList.getIfAvailable();
        if (CollectionUtils.isEmpty(interceptors)) {
            this.interceptorList = new ArrayList<>();
        } else {
            this.interceptorList = interceptors;
        }
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        String path = getUrl(request);
        try {
            return delegate.execute(request, options);
        } catch (Throwable e) {
            throw e;
        } finally {
            for (MultiProtocolClientInterceptor interceptor : interceptorList) {
                interceptor.after(path);
            }
        }
    }

    private String getUrl(Request request) {
        RequestTemplate requestTemplate = request.requestTemplate();
        String url;
        if (requestTemplate == null || StringUtils.isBlank(requestTemplate.url())) {
            url = request.url();
        } else {
            url = requestTemplate.url();
        }

        return StrUtil.subBefore(url, PARAM_SPLIT, false);
    }
}
