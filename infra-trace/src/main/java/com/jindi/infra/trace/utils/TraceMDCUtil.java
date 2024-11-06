package com.jindi.infra.trace.utils;

import org.slf4j.MDC;

import com.jindi.infra.trace.constant.TracePropagation;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.SpanContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author changbo <changbo@kuaishou.com> Created on 2020-03-02
 */
@Slf4j
public class TraceMDCUtil {

	public static void putTraceInfo(SpanContext context) {
		if (context instanceof JaegerSpanContext) {
			JaegerSpanContext jaegerSpanContext = (JaegerSpanContext) context;
			MDC.put(TracePropagation.TRACE_ID_KEY, jaegerSpanContext.getTraceId());
			MDC.put(TracePropagation.PARENT_ID_KEY, Long.toHexString(jaegerSpanContext.getParentId()));
			MDC.put(TracePropagation.SPAN_ID_KEY, Long.toHexString(jaegerSpanContext.getSpanId()));
			MDC.put(TracePropagation.SAMPLED_KEY, jaegerSpanContext.isSampled() ? "1" : "0");
		}
	}

	public static void clear() {
		MDC.clear();
	}
}
