package com.jindi.infra.cache.redis.client;


import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IHashRedisClient<K, HK, HV> {
    Long hdelete(K key, HK... hashKeys);
    Boolean hhasKey(K key, HK hashKey);
    HV hget(K key, HK hashKey);
    List<HV> hmultiGet(K key, Collection<HK> hashKeys);
    Long hincrement(K key, HK hashKey, long delta);
    Double hincrement(K key, HK hashKey, double delta);
    Long hlengthOfValue(K key, HK hashKey);
    Long hsize(K key);
    void hputAll(K key, Map<HK, HV> map);
    void hput(K key, HK hashKey, HV value);
    Boolean hputIfAbsent(K key, HK hashKey, HV value);
    List<HV> hvalues(K key);
    Map<String, HV> hentries(K key);
    Cursor<Map.Entry<String, HV>> hscan(K key, ScanOptions options);
    RedisOperations<String, ?> hgetOperations();
}
