package com.jindi.infra.cache.redis.client;


import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ISetRedisClient<K, V> {

    Long sadd(K key, V[] values);
    Long sremove(K key, V... values);
    V spop(K key);
    List<V> spop(K key, long count);
    Boolean smove(K key, V value, K destKey);
    Long ssize(K key);
    Boolean sisMember(K key, Object o);
    Set<V> sintersect(K key, K otherKey);
    Set<V> sintersect(K key, Collection otherKeys);
    Set<V> sintersect(Collection keys);
    Long sintersectAndStore(K key, K otherKey, K destKey);
    Long sintersectAndStore(K key, Collection otherKeys, K destKey);
    Long sintersectAndStore(Collection keys, K destKey);
    Set<V> sunion(K key, K otherKey);
    Set<V> sunion(K key, Collection otherKeys);
    Set<V> sunion(Collection keys);
    Long sunionAndStore(K key, K otherKey, K destKey);
    Long sunionAndStore(K key, Collection otherKeys, K destKey);
    Long sunionAndStore(Collection keys, K destKey);
    Set<V> sdifference(K key, K otherKey);
    Set<V> sdifference(K key, Collection otherKeys);
    Set<V> sdifference(Collection keys);
    Long sdifferenceAndStore(K key, K otherKey, K destKey);
    Long sdifferenceAndStore(K key, Collection otherKeys, K destKey);
    Long sdifferenceAndStore(Collection keys, K destKey);
    Set<V> smembers(K key);
    Object srandomMember(K key);
    Set<V> sdistinctRandomMembers(K key, long count);
    List<V> srandomMembers(K key, long count);
    Cursor<V> sscan(K key, ScanOptions options);
    RedisOperations<String, V> sgetOperations();
}
