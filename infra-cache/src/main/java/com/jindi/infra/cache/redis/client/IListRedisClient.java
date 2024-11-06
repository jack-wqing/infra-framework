package com.jindi.infra.cache.redis.client;


import org.springframework.data.redis.core.RedisOperations;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface IListRedisClient<K, V> {

    V lleftPop(K key, Duration timeout);
    V lrightPop(K key, Duration timeout);
    V lrightPopAndLeftPush(K sourceK, K key, Duration timeout);
    List<V> lrange(K key, long start, long end);
    void ltrim(K key, long start, long end);
    Long lsize(K key);
    Long lleftPush(K key, V value);
    Long lleftPushAll(K key, V... values);
    Long lleftPushAll(K key, Collection<V> values);
    Long lleftPushIfPresent(K key, V value);
    Long lleftPush(K key, V pivot, V value);
    Long lrightPush(K key, V value);
    Long lrightPushAll(K key, V... values);
    Long lrightPushAll(K key, Collection<V> values);
    Long lrightPushIfPresent(K key, V value);
    Long lrightPush(K key, V pivot, V value);
    void lset(K key, long index, V value);
    Long lremove(K key, long count, V value);
    V lindex(K key, long index);
    V lleftPop(K key);
    V lleftPop(K key, long timeout, TimeUnit unit);
    V lrightPop(K key);
    V lrightPop(K key, long timeout, TimeUnit unit);
    V lrightPopAndLeftPush(K sourceK, K destinationK);
    V lrightPopAndLeftPush(K sourceK, K destinationK, long timeout, TimeUnit unit);
    RedisOperations<String, V> lgetOperations();
}
