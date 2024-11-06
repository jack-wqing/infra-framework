package com.jindi.infra.cache.redis.client;


import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.Set;

public interface IZSetRedisClient<K, V> {

    Long zunionAndStore(K key, Collection<K> otherKs, K destK, RedisZSetCommands.Aggregate aggregate);
    Long zintersectAndStore(K key, Collection<K> otherKs, K destK, RedisZSetCommands.Aggregate aggregate);
    Boolean zadd(K key, V value, double score);
    Long zadd(K key, Set set);
    Long zremove(K key, V... values);
    Double zincrementScore(K key, V value, double delta);
    Long zrank(K key, Object o);
    Long zreverseRank(K key, Object o);
    Set zrange(K key, long start, long end);
    Set<ZSetOperations.TypedTuple<V>> zrangeWithScores(K key, long start, long end);
    Set zrangeByScore(K key, double min, double max);
    Set<ZSetOperations.TypedTuple<V>> zrangeByScoreWithScores(K key, double min, double max);
    Set zrangeByScore(K key, double min, double max, long offset, long count);
    Set<ZSetOperations.TypedTuple<V>> zrangeByScoreWithScores(K key, double min, double max, long offset, long count);
    Set zreverseRange(K key, long start, long end);
    Set<ZSetOperations.TypedTuple<V>> zreverseRangeWithScores(K key, long start, long end);
    Set zreverseRangeByScore(K key, double min, double max);
    Set<ZSetOperations.TypedTuple<V>> zreverseRangeByScoreWithScores(K key, double min, double max);
    Set zreverseRangeByScore(K key, double min, double max, long offset, long count);
    Set<ZSetOperations.TypedTuple<V>> zreverseRangeByScoreWithScores(K key, double min, double max, long offset, long count);
    Long zcount(K key, double min, double max);
    Long zsize(K key);
    Long zCard(K key);
    Double zscore(K key, Object o);
    Long zremoveRange(K key, long start, long end);
    Long zremoveRangeByScore(K key, double min, double max);
    Long zunionAndStore(K key, K otherK, K destK);
    Long zunionAndStore(K key, Collection<K> otherKs, K destK);
    Long zunionAndStore(K key, Collection<K> otherKs, K destK, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights);
    Long zintersectAndStore(K key, K otherK, K destK);
    Long zintersectAndStore(K key, Collection<K> otherKs, K destK);
    Long zintersectAndStore(K key, Collection<K> otherKs, K destK, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights);
    Cursor<ZSetOperations.TypedTuple<V>> zscan(K key, ScanOptions options);
    Set zrangeByLex(K key, RedisZSetCommands.Range range);
    Set zrangeByLex(K key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit);
    RedisOperations zgetOperations();
}
