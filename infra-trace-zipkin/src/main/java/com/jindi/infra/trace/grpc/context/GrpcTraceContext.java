package com.jindi.infra.trace.grpc.context;

import java.util.Map;

import javax.annotation.Resource;

import com.jindi.infra.common.constant.GrpcContextConstant;
import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.grpc.model.CallContext;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.trace.consts.GrpcConsts;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class GrpcTraceContext {

    @Resource
    private TraceContext traceContext;

    public Span buildClientTraceContext(MethodDescriptor method, Map<String, String> extHeaders) {
        TracePropagation tracePropagation = TraceMDCUtil.getCurrentTracePropagation();
        TracePropagation csTrace = traceContext.createCSTracePropagation(tracePropagation);
        fillExtHeaders(csTrace, extHeaders);
        CallContext callContext = CallContext.currentCallContext();
        return traceContext.buildSpan(csTrace, method.getFullMethodName(), Span.KindEnum.CLIENT, callContext.getServerName());
    }

    public Span buildServerTraceContext(TracePropagation tracePropagation, MethodDescriptor method, Metadata headers) {
        String rpcOriginValue = GrpcHeaderUtils.getHeaderValue(GrpcContextConstant.RPC_ORIGIN, headers);
        return traceContext.buildSpan(tracePropagation, method.getFullMethodName(), Span.KindEnum.SERVER, rpcOriginValue);
    }

    private void fillExtHeaders(TracePropagation csTrace, Map<String, String> extHeaders) {
        if (StringUtils.isNotBlank(csTrace.getTraceId())) {
            extHeaders.put(TracePropagation.TRACE_ID_KEY, csTrace.getTraceId());
        }
        if (StringUtils.isNotBlank(csTrace.getSpanId())) {
            extHeaders.put(TracePropagation.SPAN_ID_KEY, csTrace.getSpanId());
        }
        if (StringUtils.isNotBlank(csTrace.getParentSpanId())) {
            extHeaders.put(TracePropagation.PARENT_ID_KEY, csTrace.getParentSpanId());
        }
        if (StringUtils.isNotBlank(csTrace.getSampled())) {
            extHeaders.put(TracePropagation.SAMPLED_KEY, csTrace.getSampled());
        }
        if (StringUtils.isNotBlank(csTrace.getExtra())) {
            extHeaders.put(TracePropagation.EXTRA_KEY, csTrace.getExtra());
        }
    }

    public TracePropagation buildTracePropagation(Metadata headers) {
        String traceId = getTraceId(headers);
        if (StringUtils.isBlank(traceId)) {
            return traceContext.createTracePropagation();
        }
        String spanId = getSpanId(headers);
        String parentId = getParentSpanId(headers);
        String sampled = getSampled(headers);
        String extra = getExtra(headers);
        return traceContext.createTracePropagation(traceId, parentId, spanId, sampled, extra);
    }

    private String getTraceId(Metadata headers) {
        String traceId = GrpcHeaderUtils.getHeaderValue(TracePropagation.TRACE_ID_KEY, headers);
        if (StringUtils.isBlank(traceId)) {
            traceId = GrpcHeaderUtils.getHeaderValue(TracePropagation.TRACE_ID_KEY_B3, headers);
        }
        return traceId;
    }

    private String getSpanId(Metadata headers) {
        String spanId = GrpcHeaderUtils.getHeaderValue(TracePropagation.SPAN_ID_KEY, headers);
        if (StringUtils.isBlank(spanId)) {
            spanId = GrpcHeaderUtils.getHeaderValue(TracePropagation.SPAN_ID_KEY_B3, headers);
        }
        return spanId;
    }

    private String getParentSpanId(Metadata headers) {
        String parentSpanId = GrpcHeaderUtils.getHeaderValue(TracePropagation.PARENT_ID_KEY, headers);
        if (StringUtils.isBlank(parentSpanId)) {
            parentSpanId = GrpcHeaderUtils.getHeaderValue(TracePropagation.PARENT_SPAN_ID_KEY_B3, headers);
        }
        return parentSpanId;
    }

    private String getSampled(Metadata headers) {
        String sampled = GrpcHeaderUtils.getHeaderValue(TracePropagation.SAMPLED_KEY, headers);
        if (StringUtils.isBlank(sampled)) {
            sampled = GrpcHeaderUtils.getHeaderValue(TracePropagation.SAMPLED_KEY_B3, headers);
        }
        return sampled;
    }

    private String getExtra(Metadata headers) {
        String extra = GrpcHeaderUtils.getHeaderValue(TracePropagation.EXTRA_KEY, headers);
        if (StringUtils.isBlank(extra)) {
            extra = GrpcHeaderUtils.getHeaderValue(TracePropagation.EXTRA_KEY_B3, headers);
        }
        return extra;
    }
}
