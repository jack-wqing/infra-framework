package com.jindi.infra.datasource.interceptor;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.jindi.infra.datasource.config.ExecuteLimitProperties;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
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

import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
)})
@Slf4j
public class SqlQuerySizeCatInterceptor implements Interceptor {

    private ExecuteLimitProperties properties;
    private static Counter SQL_QUERY_SIZE_COUNTER;
    private String applicationName;
    private String env;

    public SqlQuerySizeCatInterceptor(String applicationName, String env, ExecuteLimitProperties properties, CollectorRegistry collectorRegistry) {
        this.properties = properties;
        this.applicationName = applicationName;
        this.env = env;
        initCounter(collectorRegistry);
    }

    private static void initCounter(CollectorRegistry collectorRegistry) {
        if (SQL_QUERY_SIZE_COUNTER != null || collectorRegistry == null) {
            return;
        }
        SQL_QUERY_SIZE_COUNTER = Counter.build()
                .name("sql_big_query_size_counter")
                .labelNames("application", "env", "mapper")
                .help("SQL大结果集打点").register(collectorRegistry);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object res = invocation.proceed();
        if (res instanceof List && ((List) res).size() >= properties.getDefaultLimit()) {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            String[] strArr = mappedStatement.getId().split("\\.");
            String methodName = strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];
            Cat.logEvent("SQL.BigResultSize", methodName, Message.SUCCESS, String.valueOf(((List)res).size()));
            inc(methodName);
        }
        return res;
    }

    private void inc(String method) {
        if (SQL_QUERY_SIZE_COUNTER == null) {
            return;
        }
        SQL_QUERY_SIZE_COUNTER.labels(applicationName, env, method).inc();
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
