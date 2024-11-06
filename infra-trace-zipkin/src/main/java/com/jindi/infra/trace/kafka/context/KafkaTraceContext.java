package com.jindi.infra.trace.kafka.context;

import java.nio.charset.Charset;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;

public class KafkaTraceContext {

    @Resource
    private TraceContext traceContext;

    public KafkaTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public void buildConsumerTracePropagation(Headers headers) {
        String traceId = getTraceId(headers);
        if (StringUtils.isBlank(traceId)) {
            traceContext.createTracePropagation();
            return;
        }
        String spanId = getSpanId(headers);
        String parentId = getParentSpanId(headers);
        String sampled = getSampled(headers);
        String extra = getExtra(headers);
        traceContext.createTracePropagation(traceId, parentId, spanId, sampled, extra);
    }

    public void buildNewConsumerTracePropagation() {
        traceContext.createTracePropagation();
    }

    private String getTraceId(Headers headers) {
        return getTarget(headers, TracePropagation.TRACE_ID_KEY, TracePropagation.TRACE_ID_KEY_B3);
    }

    private String getSpanId(Headers headers) {
        return getTarget(headers, TracePropagation.SPAN_ID_KEY, TracePropagation.SPAN_ID_KEY_B3);
    }

    private String getParentSpanId(Headers headers) {
        return getTarget(headers, TracePropagation.PARENT_ID_KEY, TracePropagation.PARENT_SPAN_ID_KEY_B3);
    }

    private String getSampled(Headers headers) {
        return getTarget(headers, TracePropagation.SAMPLED_KEY, TracePropagation.SAMPLED_KEY_B3);
    }

    private String getExtra(Headers headers) {
        return getTarget(headers, TracePropagation.EXTRA_KEY, TracePropagation.EXTRA_KEY_B3);
    }

    private String getTarget(Headers headers, String key, String keyB3s) {
        Header header = headers.lastHeader(key);
        if (header == null) {
            header = headers.lastHeader(keyB3s);
        }
        if (header == null) {
            return null;
        }
        return new String(header.value(), Charset.defaultCharset());
    }

    public void buildProducerTracePropagation(TracePropagation tracePropagation, Headers headers) {
        TracePropagation csTrace = traceContext.createCSTracePropagation(tracePropagation);
        fillHeaders(headers, csTrace);
    }

    private void fillHeaders(Headers headers, TracePropagation csTrace) {
        String traceId = csTrace.getTraceId();
        if (StringUtils.isNotBlank(traceId)) {
            headers.add(TracePropagation.TRACE_ID_KEY, traceId.getBytes());
        }

        String parentId = csTrace.getParentSpanId();
        if (StringUtils.isNotBlank(parentId)) {
            headers.add(TracePropagation.PARENT_ID_KEY, parentId.getBytes());
        }

        String spanId = csTrace.getSpanId();
        if (StringUtils.isNotBlank(spanId)) {
            headers.add(TracePropagation.SPAN_ID_KEY, spanId.getBytes());
        }

        String sampled = csTrace.getSampled();
        if (StringUtils.isNotBlank(sampled)) {
            headers.add(TracePropagation.SAMPLED_KEY, sampled.getBytes());
        }

        String extra = csTrace.getExtra();
        if (StringUtils.isNotBlank(extra)) {
            headers.add(TracePropagation.EXTRA_KEY, extra.getBytes());
        }
    }
}
