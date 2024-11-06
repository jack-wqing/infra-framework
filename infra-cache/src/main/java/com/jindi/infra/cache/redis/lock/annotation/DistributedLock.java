package com.jindi.infra.cache.redis.lock.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 键表达式；通过Spel语法生成键
     *
     * @return
     */
    String keyExpression();

    /**
     * 超时时间
     *
     * @return
     */
    int timeoutMillis();

    /**
     * 阻塞，直到获取到锁或者超时
     *
     * @return
     */
    boolean block() default false;
}
