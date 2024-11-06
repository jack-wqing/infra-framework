package com.jindi.infra.trace.dubbo.context;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;

public class DubboTraceContext {

    private static final String DUBBO_TRACE_ID_KEY = "X-B3-TraceId-Jindi";
    private static final String DUBBO_SPAN_ID_KEY = "X-B3-SpanId-Jindi";
    private static final String DUBBO_PARENT_ID_KEY = "X-B3-ParentSpanId-Jindi";
    private static final String DUBBO_SAMPLED_KEY = "X-B3-Sampled-Jindi";
    private static final String DUBBO_EXTRA_KEY = "X-B3-Extra-Jindi";

    @Resource
    private TraceContext traceContext;

    public Span buildClientTraceSpan(Invocation invocation, Invoker<?> invoker, String remoteServiceName) {
        TracePropagation tracePropagation = TraceMDCUtil.getCurrentTracePropagation();
        TracePropagation csTrace = traceContext.createCSTracePropagation(tracePropagation);
        fillAttachments(invocation, csTrace);
        String name = String.format("%s/%s", invoker.getUrl().getPath(), invocation.getMethodName());
        return traceContext.buildSpan(csTrace, name, Span.KindEnum.CLIENT, remoteServiceName);
    }

    public Span buildServerTraceSpan(Invocation invocation, Invoker<?> invoker, String remoteServiceName) {
        TracePropagation tracePropagation = buildTracePropagation(invocation);
        String name = String.format("%s/%s", invoker.getUrl().getPath(), invocation.getMethodName());
        return traceContext.buildSpan(tracePropagation, name, Span.KindEnum.SERVER, remoteServiceName);
    }

    private void fillAttachments(Invocation invocation, TracePropagation tracePropagation) {
        Map<String, String> attachments = invocation.getAttachments();
        if (StringUtils.isNotBlank(tracePropagation.getTraceId())) {
            attachments.put(TracePropagation.TRACE_ID_KEY, tracePropagation.getTraceId());
            attachments.put(DUBBO_TRACE_ID_KEY, tracePropagation.getTraceId());
        }
        if (StringUtils.isNotBlank(tracePropagation.getParentSpanId())) {
            attachments.put(TracePropagation.PARENT_ID_KEY, tracePropagation.getParentSpanId());
            attachments.put(DUBBO_PARENT_ID_KEY, tracePropagation.getParentSpanId());
        }
        if (StringUtils.isNotBlank(tracePropagation.getSpanId())) {
            attachments.put(TracePropagation.SPAN_ID_KEY, tracePropagation.getSpanId());
            attachments.put(DUBBO_SPAN_ID_KEY, tracePropagation.getSpanId());
        }
        if (StringUtils.isNotBlank(tracePropagation.getSampled())) {
            attachments.put(TracePropagation.SAMPLED_KEY, tracePropagation.getSampled());
            attachments.put(DUBBO_SAMPLED_KEY, tracePropagation.getSampled());
        }
        if (StringUtils.isNotBlank(tracePropagation.getExtra())) {
            attachments.put(TracePropagation.EXTRA_KEY, tracePropagation.getExtra());
            attachments.put(DUBBO_EXTRA_KEY, tracePropagation.getExtra());
        }
    }

    private TracePropagation buildTracePropagation(Invocation invocation) {
        String traceId = getTraceId(invocation);
        if (StringUtils.isBlank(traceId)) {
            return traceContext.createTracePropagation();
        }
        String spanId = getSpanId(invocation);
        String parentId = getParentSpanId(invocation);
        String sampled = getSampled(invocation);
        String extra = getExtra(invocation);
        return traceContext.createTracePropagation(traceId, parentId, spanId, sampled, extra);
    }

    private String getTraceId(Invocation invocation) {
        String traceId = invocation.getAttachment(TracePropagation.TRACE_ID_KEY);
        if (StringUtils.isBlank(traceId)) {
            traceId = invocation.getAttachment(TracePropagation.TRACE_ID_KEY_B3);
        }
        return traceId;
    }

    private String getSpanId(Invocation invocation) {
        String spanId = invocation.getAttachment(TracePropagation.SPAN_ID_KEY);
        if (StringUtils.isBlank(spanId)) {
            spanId = invocation.getAttachment(TracePropagation.SPAN_ID_KEY_B3);
        }
        return spanId;
    }

    private String getParentSpanId(Invocation invocation) {
        String parentSpanId = invocation.getAttachment(TracePropagation.PARENT_ID_KEY);
        if (StringUtils.isBlank(parentSpanId)) {
            parentSpanId = invocation.getAttachment(TracePropagation.PARENT_SPAN_ID_KEY_B3);
        }
        return parentSpanId;
    }

    private String getSampled(Invocation invocation) {
        String sampled = invocation.getAttachment(TracePropagation.SAMPLED_KEY);
        if (StringUtils.isBlank(sampled)) {
            sampled = invocation.getAttachment(TracePropagation.SAMPLED_KEY_B3);
        }
        return sampled;
    }

    private String getExtra(Invocation invocation) {
        String extra = invocation.getAttachment(TracePropagation.EXTRA_KEY);
        if (StringUtils.isBlank(extra)) {
            extra = invocation.getAttachment(TracePropagation.EXTRA_KEY_B3);
        }
        return extra;
    }
}
