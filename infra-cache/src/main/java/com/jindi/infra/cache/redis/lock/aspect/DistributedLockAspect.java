package com.jindi.infra.cache.redis.lock.aspect;

import com.jindi.infra.cache.redis.lock.DistributedLockService;
import com.jindi.infra.cache.redis.lock.annotation.DistributedLock;
import com.jindi.infra.cache.redis.lock.exception.DistributedLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Aspect
@Slf4j
public class DistributedLockAspect {

    private static final String ARGS = "args";
    private DistributedLockService distributedLockService;

    public DistributedLockAspect(DistributedLockService distributedLockService) {
        this.distributedLockService = distributedLockService;
    }

    @Around("@annotation(distributedLock)")
    public Object doAround(ProceedingJoinPoint pjp, DistributedLock distributedLock) throws Throwable {
        String keyExpression = distributedLock.keyExpression();
        int timeoutMillis = distributedLock.timeoutMillis();
        if (StringUtils.isBlank(keyExpression) || timeoutMillis <= 0) {
            return pjp.proceed();
        }
        String key = null;
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable(ARGS, pjp.getArgs());
            ExpressionParser expressionParser = new SpelExpressionParser();
            Expression expression = expressionParser.parseExpression(keyExpression);
            key = expression.getValue(context, String.class);
            boolean r = distributedLock.block()
                ? distributedLockService.lock(key, timeoutMillis)
                : distributedLockService.tryLock(key, timeoutMillis);
            if (r) {
                log.info("获取【{}】 key = {} 成功；锁超时 {} 毫秒", distributedLock.block() ? "悲观锁" : "乐观锁", key, timeoutMillis);
                return pjp.proceed();
            } else {
                log.info("获取【{}】 key = {} 失败；锁超时 {} 毫秒", distributedLock.block() ? "悲观锁" : "乐观锁", key, timeoutMillis);
            }
        } finally {
            if (key != null) {
                try {
                    distributedLockService.unlock(key);
                } catch (Throwable e) {
                    log.warn("unlock distributed lock", e);
                }
            }
        }
        throw new DistributedLockException("lock distributed lock failure");
    }
}
