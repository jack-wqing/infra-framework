package com.jindi.infra.trace.feign.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.utils.TraceUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;

import feign.Request;
import feign.Response;

public class InfraTraceLoadBalanceFeignClient extends LoadBalancerFeignClient {

    private LoadBalancerFeignClient delegate;
    private TraceContext traceContext;

    public InfraTraceLoadBalanceFeignClient(LoadBalancerFeignClient loadBalancerFeignClient, CachingSpringLoadBalancerFactory lbClientFactory,
                                            SpringClientFactory clientFactory, TraceContext traceContext) {
        super(loadBalancerFeignClient.getDelegate(), lbClientFactory, clientFactory);
        this.delegate = loadBalancerFeignClient;
        this.traceContext = traceContext;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        TracePropagation currentTracePropagation = TraceMDCUtil.getCurrentTracePropagation();
        TracePropagation csTrace = traceContext.createCSTracePropagation(currentTracePropagation);
        Request req = buildNewRequest(request, csTrace);
        Span span = createSpan(request, csTrace);
        Response response;
        try {
            response = delegate.execute(req, options);
        } catch (Throwable e) {
            TraceUtil.tag(span, TagsConsts.ERROR, e.getMessage());
            throw e;
        } finally {
            traceContext.writeSpan(span);
        }
        return response;
    }

    private Span createSpan(Request request, TracePropagation csTrace) {
        if (StringUtils.isBlank(request.url())) {
            return traceContext.buildSpan(csTrace, request.url(), Span.KindEnum.CLIENT, "");
        }
        URI uri = URI.create(request.url());
        return traceContext.buildSpan(csTrace, uri.getPath(), Span.KindEnum.CLIENT, uri.getHost());
    }

    private Request buildNewRequest(Request request, TracePropagation csTrace) {
        Map<String, Collection<String>> headers = request.headers();
        headers = addTraceHeader(headers, csTrace);
        return Request.create(request.httpMethod(), request.url(), headers, request.body(), request.charset(), request.requestTemplate());
    }

    private Map<String, Collection<String>> addTraceHeader(Map<String, Collection<String>> headers, TracePropagation csTrace) {
        // headers的map类型不允许变更
        Map<String, Collection<String>> map = new HashMap<>(headers);
        if (StringUtils.isNotBlank(csTrace.getTraceId())) {
            map.put(TracePropagation.TRACE_ID_KEY, Collections.singletonList(csTrace.getTraceId()));
        }
        if (StringUtils.isNotBlank(csTrace.getParentSpanId())) {
            map.put(TracePropagation.PARENT_ID_KEY, Collections.singletonList(csTrace.getParentSpanId()));
        }
        if (StringUtils.isNotBlank(csTrace.getSpanId())) {
            map.put(TracePropagation.SPAN_ID_KEY, Collections.singletonList(csTrace.getSpanId()));
        }
        if (StringUtils.isNotBlank(csTrace.getSampled())) {
            map.put(TracePropagation.SAMPLED_KEY, Collections.singletonList(csTrace.getSampled()));
        }
        if (StringUtils.isNotBlank(csTrace.getExtra())) {
            map.put(TracePropagation.EXTRA_KEY, Collections.singletonList(csTrace.getExtra()));
        }
        if (StringUtils.isNotBlank(traceContext.getServiceName())) {
            map.put(TracePropagation.APPLICATION_NAME, Collections.singleton(traceContext.getServiceName()));
        }
        return map;
    }
}
