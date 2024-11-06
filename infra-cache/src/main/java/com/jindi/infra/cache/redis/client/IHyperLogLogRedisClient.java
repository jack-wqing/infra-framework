package com.jindi.infra.cache.redis.client;


public interface IHyperLogLogRedisClient<K, V> {

    Long hyperadd(K key, V... values);

    Long hypersize(K... keys);

    Long hyperunion(K destination, K... sourceKs);

    void hyperdelete(K key);
}
