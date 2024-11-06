package com.jindi.infra.datasource.interceptor;

import static com.jindi.infra.common.util.InnerLogUtils.printDebugLog;

import java.util.Map;
import java.util.Properties;

import com.jindi.infra.datasource.config.ExecuteLimitProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.jindi.infra.datasource.config.ExecuteTimeoutProperties;

import lombok.extern.slf4j.Slf4j;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
)})
@Slf4j
public class SqlQueryLimitCountInterceptor implements Interceptor {

    private ExecuteLimitProperties properties;

    public SqlQueryLimitCountInterceptor(ExecuteLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if(!properties.getLimitSwitch()) {
            return invocation.proceed();
        }
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        String method = ms.getId();

        printDebugLog("interceptor method: {}", method);
        Integer limitCount = getLimitCount(method);
        if (limitCount == null) {
            limitCount = properties.getDefaultLimit();
        }
        printDebugLog("interceptor method: {} limitCount: {}", method, limitCount);
        if (limitCount <= 0) {
            return invocation.proceed();
        }
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds)args[2];
        ResultHandler resultHandler = (ResultHandler)args[3];
        Executor executor = (Executor)invocation.getTarget();

        CacheKey cacheKey;
        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey)args[4];
            boundSql = (BoundSql)args[5];
        }
        BoundSql newBoundSql = rewriteBoundSql(ms, limitCount, boundSql);
        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, newBoundSql);
    }

    /**
     * 改写sql语句，增加limit兜底值
     */
    private BoundSql rewriteBoundSql(MappedStatement ms, Integer limitCount, BoundSql boundSql) {
        String sql = boundSql.getSql();
        if(sql.contains("limit")) {
            return boundSql;
        }
        String rewriteSql = sql + " limit " + limitCount;
        printDebugLog("interceptor rewrite origin sql: {}, rewrite sql: {}", sql, rewriteSql);
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), rewriteSql, boundSql.getParameterMappings(),
                boundSql.getParameterObject());
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }

    /**
     * 获取method对应的limit配置，优先精确匹配，然后通配符匹配
     */
    private Integer getLimitCount(String method) {
        Map<String, Integer> methodLimitConfig = properties.getMethodLimitConfig();
        if (methodLimitConfig.containsKey(method)) {
            return methodLimitConfig.get(method);
        }
        String wildcardMethod = getWildcardMethod(method);
        if (StringUtils.isNotBlank(wildcardMethod) && methodLimitConfig.containsKey(wildcardMethod)) {
            return methodLimitConfig.get(wildcardMethod);
        }
        return null;
    }

    /**
     * 获取通配符方法
     */
    private String getWildcardMethod(String method) {
        if (method.contains(".")) {
            return method.substring(0, method.lastIndexOf(".") + 1) + "*";
        }
        return null;
    }
}
