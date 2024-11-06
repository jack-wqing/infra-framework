package com.jindi.infra.trace.mysql;

import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.jindi.infra.trace.utils.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
)})
@Slf4j
public class SqlTracingInterceptor implements Interceptor {

    private String datasource;

    private TraceContext traceContext;

    private String jdbc;

    public SqlTracingInterceptor(String datasource, String jdbc, TraceContext traceContext) {
        this.datasource = "mysql-" + datasource;
        this.traceContext = traceContext;
        this.jdbc = jdbc;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String[] strArr = mappedStatement.getId().split("\\.");
        String methodName = strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];
        TracePropagation currentTracePropagation = TraceMDCUtil.getCurrentTracePropagation();
        TracePropagation trace = traceContext.createCSTracePropagation(currentTracePropagation);
        Span span = createSpan(trace, methodName, getSql(invocation, mappedStatement));
        try {
            return invocation.proceed();
        } catch (Throwable e) {
            TraceUtil.tag(span, TagsConsts.ERROR, e.getMessage());
            throw e;
        } finally {
            traceContext.writeSpan(span);
        }
    }

    private Span createSpan(TracePropagation csTrace, String methodName, String sql) {
        Span span = traceContext.buildSpan(csTrace, methodName, Span.KindEnum.CLIENT, datasource);
        TraceUtil.tag(span, "http.method", sql);
        TraceUtil.tag(span, "http.url", jdbc);
        return span;
    }

    private String getSql(Invocation invocation, MappedStatement ms) {
        Object[] args = invocation.getArgs();
        Object parameter = args[1];
        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter);
        } else {
            boundSql = (BoundSql)args[5];
        }
        return boundSql.getSql();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
