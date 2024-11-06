package com.jindi.infra.cache.redis.interceptor;


import com.jindi.infra.cache.redis.key.Key;

public abstract class RedisInterceptor {

    public void doBefore(String opt, Key key, String connection) {}

    public void doAfter(String opt, Key key, String connection) {}

    public void doError(String opt, Key key, String connection, Throwable e) {}

    public void doFinally(String opt, Key key, String connection, Object result) {}

}
