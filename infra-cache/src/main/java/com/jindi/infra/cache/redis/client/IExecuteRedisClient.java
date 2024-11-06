package com.jindi.infra.cache.redis.client;


import io.micrometer.core.lang.Nullable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Closeable;
import java.util.List;

public interface IExecuteRedisClient<V>  {
    <T> T execute(RedisCallback<T> action);
    <T> T execute(RedisCallback<T> action, boolean exposeConnection);
    <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline);
    <T> T execute(SessionCallback<T> session);
    List<Object> executePipelined(SessionCallback<?> session);
    List<Object> executePipelined(SessionCallback<?> session, @Nullable RedisSerializer<?> resultSerializer);
    List<Object> executePipelined(RedisCallback<?> action);
    List<Object> executePipelined(RedisCallback<?> action, @Nullable RedisSerializer<?> resultSerializer);
    <T> T execute(RedisScript<T> script, List<String> keys, Object... args);
    <T extends Closeable> T executeWithStickyConnection(RedisCallback<T> callback);
}
