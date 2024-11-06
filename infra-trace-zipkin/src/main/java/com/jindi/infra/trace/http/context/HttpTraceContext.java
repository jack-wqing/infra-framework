package com.jindi.infra.trace.http.context;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;

public class HttpTraceContext {

    @Resource
    private TraceContext traceContext;

    public Span buildServerTraceSpan(HttpServletRequest request) {
        TracePropagation tracePropagation = buildTracePropagation(request);
        String remoteServiceName = request.getHeader(TracePropagation.APPLICATION_NAME);
        if (StringUtils.isBlank(remoteServiceName)) {
            remoteServiceName = String.format("%s:%s", request.getRemoteAddr(), request.getRemotePort());
        }
        return traceContext.buildSpan(tracePropagation, request.getRequestURI(), Span.KindEnum.SERVER, remoteServiceName);
    }

    private TracePropagation buildTracePropagation(HttpServletRequest request) {
        String traceId = getTraceId(request);
        if (StringUtils.isBlank(traceId)) {
            return traceContext.createTracePropagation();
        }
        String parentId = getParentId(request);
        String spanId = getSpanId(request);
        String sampled = getSampled(request);
        String extra = getExtra(request);
        return traceContext.createTracePropagation(traceId, parentId, spanId, sampled, extra);
    }

    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TracePropagation.TRACE_ID_KEY);
        if (StringUtils.isBlank(traceId)) {
            traceId = request.getHeader(TracePropagation.TRACE_ID_KEY_B3);
        }
        return traceId;
    }

    private String getParentId(HttpServletRequest request) {
        String spanId = request.getHeader(TracePropagation.PARENT_ID_KEY);
        if (StringUtils.isBlank(spanId)) {
            spanId = request.getHeader(TracePropagation.PARENT_SPAN_ID_KEY_B3);
        }
        return spanId;
    }

    private String getSpanId(HttpServletRequest request) {
        String spanId = request.getHeader(TracePropagation.SPAN_ID_KEY);
        if (StringUtils.isBlank(spanId)) {
            spanId = request.getHeader(TracePropagation.SPAN_ID_KEY_B3);
        }
        return spanId;
    }

    private String getSampled(HttpServletRequest request) {
        String sampled = request.getHeader(TracePropagation.SAMPLED_KEY);
        if (StringUtils.isBlank(sampled)) {
            sampled = request.getHeader(TracePropagation.SAMPLED_KEY_B3);
        }
        return sampled;
    }

    private String getExtra(HttpServletRequest request) {
        String extra = request.getHeader(TracePropagation.EXTRA_KEY);
        if (StringUtils.isBlank(extra)) {
            extra = request.getHeader(TracePropagation.EXTRA_KEY_B3);
        }
        return extra;
    }
}
