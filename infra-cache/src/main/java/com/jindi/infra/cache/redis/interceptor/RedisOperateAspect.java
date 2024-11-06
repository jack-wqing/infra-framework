package com.jindi.infra.cache.redis.interceptor;

import com.dianping.cat.Cat;
import com.google.common.collect.Lists;
import com.jindi.infra.cache.redis.client.TycRedisClient;
import com.jindi.infra.cache.redis.key.Key;
import com.jindi.infra.cache.redis.metrics.BaseTycRedisClientMetrics;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Aspect
public class RedisOperateAspect {

    @Autowired(required = false)
    private List<RedisInterceptor> redisInterceptorList;

    @Autowired(required = false)
    private List<BaseTycRedisClientMetrics> baseTycRedisClientMetrics;

    public static Map<String, String> beanName2Connection = new HashMap<>();

    private static final List<String> IGNORE_OPT = Lists.newArrayList("getConnectionFactory");

    private static ThreadLocal<Boolean> INTERCEPTED_TAG = new ThreadLocal<>();

    @Around("target(com.jindi.infra.cache.redis.client.TycRedisClient) || target(org.springframework.data.redis.core.RedisTemplate)")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (CollectionUtils.isEmpty(redisInterceptorList) || CollectionUtils.isEmpty(baseTycRedisClientMetrics)) {
            return proceedingJoinPoint.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        String methodName = methodSignature.getName();
        if (IGNORE_OPT.contains(methodName) || hadIntercepted()) {
            return proceedingJoinPoint.proceed();
        }
        String connection = getConnectionConfig(proceedingJoinPoint.getTarget());
        Key key = getKey(proceedingJoinPoint, methodSignature);
        before(methodName, key, connection);
        Object result = null;
        try {
            result = proceedingJoinPoint.proceed();
            after(methodName, key, connection);
        } catch (Throwable throwable) {
            error(methodName, key, connection, throwable);
            throw throwable;
        } finally {
            doFinally(methodName, key, connection, result);
            INTERCEPTED_TAG.remove();
        }
        return result;
    }

    private Boolean hadIntercepted() {
        Boolean value = INTERCEPTED_TAG.get();
        if (value != null) {
            return true;
        }
        INTERCEPTED_TAG.set(true);
        return false;
    }

    private String getConnectionConfig(Object target) {
        if (target instanceof TycRedisClient) {
            return getConnectionConfig(((TycRedisClient) target).getTycRedisHolder());
        }
        if (target instanceof RedisTemplate) {
            return getConnectionConfig(((RedisTemplate) target));
        }
        return "unknown";
    }

    private Key getKey(ProceedingJoinPoint proceedingJoinPoint, MethodSignature methodSignature) {
        String[] parameters = methodSignature.getParameterNames();
        int paramIndex = ArrayUtils.indexOf(parameters,"key");
        if (paramIndex < 0 || proceedingJoinPoint.getArgs().length <= paramIndex) {
            return null;
        }
        Object obj = proceedingJoinPoint.getArgs()[paramIndex];
        if (obj instanceof Key) {
            return (Key) obj;
        }
        return null;
    }

    private void before(String methodName, Key key, String connection) {
        for (RedisInterceptor redisInterceptor : redisInterceptorList) {
            redisInterceptor.doBefore(methodName, key, connection);
        }
    }

    private void after(String methodName, Key key, String connection) {
        for (RedisInterceptor redisInterceptor : redisInterceptorList) {
            redisInterceptor.doAfter(methodName, key, connection);
        }
    }

    private void error(String methodName, Key key, String connection, Throwable e) {
        for (RedisInterceptor redisInterceptor : redisInterceptorList) {
            redisInterceptor.doError(methodName, key, connection, e);
        }
    }

    private void doFinally(String methodName, Key key, String connection, Object result) {
        for (RedisInterceptor redisInterceptor : redisInterceptorList) {
            redisInterceptor.doFinally(methodName, key, connection, result);
        }
    }

    private String getConnectionConfig(TycRedisHolder holder) {
        if (holder == null) {
            return "unknown";
        }
        if (beanName2Connection.containsKey(holder.getBeanName())) {
            return beanName2Connection.get(holder.getBeanName());
        }
        beanName2Connection.put(holder.getBeanName(), holder.getConnectInfo());
        return beanName2Connection.get(holder.getBeanName());
    }

    private String getConnectionConfig(RedisTemplate redisTemplate) {
        Optional<String> conn = baseTycRedisClientMetrics.stream().map(metric -> metric.getConnection(redisTemplate)).filter(StringUtils::isNotBlank).findFirst();
        return conn.isPresent() ? conn.get() : "unknown";
    }

}
