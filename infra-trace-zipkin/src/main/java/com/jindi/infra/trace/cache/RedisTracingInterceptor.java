package com.jindi.infra.trace.cache;


import com.jindi.infra.cache.redis.interceptor.RedisInterceptor;
import com.jindi.infra.cache.redis.key.Key;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.jindi.infra.trace.utils.TraceUtil;

public class RedisTracingInterceptor extends RedisInterceptor {

    private static ThreadLocal<Span> spanThreadLocal = new ThreadLocal<>();

    private TraceContext traceContext;

    public RedisTracingInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public static final String CACHE_PREFIX = "redis-";

    @Override
    public void doBefore(String opt, Key key, String connection) {
        if (spanThreadLocal.get() != null) {
            return;
        }
        TracePropagation currentTracePropagation = TraceMDCUtil.getCurrentTracePropagation();
        TracePropagation trace = traceContext.createCSTracePropagation(currentTracePropagation);
        Span span = null;
        if (key != null) {
            span = createSpan(trace, CACHE_PREFIX + key.getTemplate() + ":(" + opt + ")", connection);
        } else {
            span = createSpan(trace, CACHE_PREFIX + opt, connection);
        }
        spanThreadLocal.set(span);
    }

    private Span createSpan(TracePropagation trace, String query, String connection) {
        Span span = traceContext.buildSpan(trace, query, Span.KindEnum.CLIENT, connection);
        return span;
    }

    @Override
    public void doError(String opt, Key key, String connection, Throwable e) {
        if (spanThreadLocal.get() == null) {
            return;
        }
        TraceUtil.tag(spanThreadLocal.get(), TagsConsts.ERROR, e.getMessage());
    }

    @Override
    public void doFinally(String opt, Key key, String connection, Object result) {
        if (spanThreadLocal.get() == null) {
            return;
        }
        traceContext.writeSpan(spanThreadLocal.get());
        spanThreadLocal.remove();
    }

}
