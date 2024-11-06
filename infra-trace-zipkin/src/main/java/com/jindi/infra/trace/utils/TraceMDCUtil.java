package com.jindi.infra.trace.utils;

import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.trace.propagation.TracePropagation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * @author changbo <changbo@kuaishou.com>
 * Created on 2020-03-02
 */
@Slf4j
public class TraceMDCUtil {

    public static void putTraceInfo(TracePropagation tracePropagation) {
        MDC.put(TracePropagation.TRACE_ID_KEY, tracePropagation.getTraceId());
        MDC.put(TracePropagation.PARENT_ID_KEY, tracePropagation.getParentSpanId());
        MDC.put(TracePropagation.SPAN_ID_KEY, tracePropagation.getSpanId());
        MDC.put(TracePropagation.SAMPLED_KEY, tracePropagation.getSampled());
        MDC.put(TracePropagation.EXTRA_KEY, tracePropagation.getExtra());

        MDC.put(TracePropagation.TRACE_ID_KEY_B3, tracePropagation.getTraceId());
        MDC.put(TracePropagation.PARENT_SPAN_ID_KEY_B3, tracePropagation.getParentSpanId());
        MDC.put(TracePropagation.SPAN_ID_KEY_B3, tracePropagation.getSpanId());
        MDC.put(TracePropagation.SAMPLED_KEY_B3, tracePropagation.getSampled());
        MDC.put(TracePropagation.EXTRA_KEY_B3, tracePropagation.getExtra());
    }

    public static void putTraceInfo(com.jindi.infra.common.param.TracePropagation tracePropagation) {
        MDC.put(TracePropagation.TRACE_ID_KEY, tracePropagation.getTraceId());
        MDC.put(TracePropagation.PARENT_ID_KEY, tracePropagation.getParentSpanId());
        MDC.put(TracePropagation.SPAN_ID_KEY, tracePropagation.getSpanId());
        MDC.put(TracePropagation.SAMPLED_KEY, tracePropagation.getSampled());
        MDC.put(TracePropagation.EXTRA_KEY, tracePropagation.getExtra());

        MDC.put(TracePropagation.TRACE_ID_KEY_B3, tracePropagation.getTraceId());
        MDC.put(TracePropagation.PARENT_SPAN_ID_KEY_B3, tracePropagation.getParentSpanId());
        MDC.put(TracePropagation.SPAN_ID_KEY_B3, tracePropagation.getSpanId());
        MDC.put(TracePropagation.SAMPLED_KEY_B3, tracePropagation.getSampled());
        MDC.put(TracePropagation.EXTRA_KEY_B3, tracePropagation.getExtra());
    }

    public static void clean() {
        MDC.clear();
    }

    public static TracePropagation getCurrentTracePropagation() {
        TracePropagation tracePropagation = new TracePropagation();
        tracePropagation.setTraceId(getKey(TracePropagation.TRACE_ID_KEY));
        tracePropagation.setParentSpanId(getKey(TracePropagation.PARENT_ID_KEY));
        tracePropagation.setSpanId(getKey(TracePropagation.SPAN_ID_KEY));
        tracePropagation.setSampled(getKey(TracePropagation.SAMPLED_KEY));
        tracePropagation.setExtra(getKey(TracePropagation.EXTRA_KEY));
        return tracePropagation;
    }

    public static String getKey(String key) {
        String value = MDC.get(key);
        if (StringUtils.isBlank(value)) {
            value = ContextUtils.getContextValue(key);
            if (StringUtils.isNotBlank(value)) {
                MDC.put(key, value);
            }
        }
        return value;
    }
}
