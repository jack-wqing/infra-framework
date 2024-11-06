package com.jindi.infra.cache.redis.client;


import com.jindi.infra.cache.redis.key.Key;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisOperations;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface IValueRedisClient<K, V> {

    void set(K key, V value, Duration timeout);
    Boolean setIfAbsent(K key, V value, Duration timeout);
    Boolean setIfPresent(K key, V value, Duration timeout);
    void set(K key, V value);
    void set(K key, V value, long timeout, TimeUnit unit);
    void set(K key, V value, long seconds);
    Boolean setIfAbsent(K key, V value);
    Boolean setIfAbsent(K key, V value, long timeout, TimeUnit unit);
    Boolean setIfPresent(K key, V value);
    Boolean setIfPresent(K key, V value, long timeout, TimeUnit unit);
    void multiSet(Map<K, V> map);
    Boolean multiSetIfAbsent(Map<K, V> map);
    Object get(K key);
    Object getAndSet(K key, V value);
    List multiGet(Collection<Key> keys);
    Long increment(K key);
    Long increment(K key, long delta);
    Double increment(K key, double delta);
    Long decrement(K key);
    Long decrement(K key, long delta);
    Integer append(K key, String value);
    String get(K key, long start, long end);
    Long size(K key);
    Boolean setBit(K key, long offset, boolean value);
    Boolean getBit(K key, long offset);
    List<Long> bitField(K key, BitFieldSubCommands subCommands);
    RedisOperations getOperations();
    Boolean setIfPresent(Key key, V value, long seconds);
}
