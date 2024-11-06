package com.jindi.infra.trace.utils;

import com.dianping.cat.Cat;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.propagation.TracePropagation;

/**
 * 对外提供获取Trace工具类
 */
public class TraceUtil {

    public static String getTraceId() {
        return TraceMDCUtil.getKey(TracePropagation.TRACE_ID_KEY);
    }

    public static void tag(Span span, String key, String value) {
        if (span != null) {
            span.tag(key, value);
        }
    }

    public static void tagCatMessageId(Span span) {
        tag(span, TagsConsts.CAT_MESSAGE, Cat.getCurrentMessageId());
    }
}
