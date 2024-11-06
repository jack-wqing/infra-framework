package com.jindi.infra.cache.redis.client;


import com.jindi.infra.cache.redis.exception.TycRedisException;
import com.jindi.infra.cache.redis.key.Key;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseTycRedisClient<V> implements
        IBaseGeoRedisClient<Key, V>,
        IZSetRedisClient<Key, V>,
        IValueRedisClient<Key, V>,
        ISetRedisClient<Key, V>,
        IHyperLogLogRedisClient<Key, V>,
        IHashRedisClient<Key, String, V>,
        IListRedisClient<Key, V>,
        IExecuteRedisClient<V> ,
        IRedisTemplateClient<V> {

    public BaseTycRedisClient(RedisTemplate<String, V> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public BaseTycRedisClient() {
    }

    private RedisTemplate<String, V> redisTemplate;

    private StringRedisTemplate stringRedisTemplate;

    public void setRedisTemplate(RedisTemplate<String, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisTemplate<String, V> getRedisTemplate() {
        return redisTemplate;
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }

    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private List<String> toListKey(Collection<Key> keys) {
        if (keys == null) {
            return null;
        }
        return keys.stream().map(Key::getKey).collect(Collectors.toList());
    }

    private String[] toListKey(Key[] keys) {
        if (keys == null) {
            return null;
        }
        return Arrays.stream(keys).map(Key::getKey).toArray(String[]::new);
    }

    /**
     * Geo
     */

    @Override
    public Long ggeoAdd(Key key, Point point, V member) {
        return redisTemplate.opsForGeo().geoAdd(key.getKey(), point, member);
    }

    @Override
    public Long ggeoAdd(Key key, RedisGeoCommands.GeoLocation<V> location) {
        return redisTemplate.opsForGeo().geoAdd(key.getKey(), location);
    }

    @Override
    public Long ggeoAdd(Key key, Map<V, Point> memberCoordinateMap) {
        return redisTemplate.opsForGeo().geoAdd(key.getKey(), memberCoordinateMap);
    }

    @Override
    public Long ggeoAdd(Key key, Iterable<RedisGeoCommands.GeoLocation<V>> geoLocations) {
        return redisTemplate.opsForGeo().geoAdd(key.getKey(), geoLocations);
    }

    @Override
    public Distance ggeoDist(Key key, V member1, V member2) {
        return redisTemplate.opsForGeo().geoDist(key.getKey(), member1, member2);
    }

    @Override
    public Distance ggeoDist(Key key, V member1, V member2, Metric metric) {
        return redisTemplate.opsForGeo().geoDist(key.getKey(), member1, member2, metric);
    }

    @Override
    public List<String> ggeoHash(Key key, V... members) {
        return redisTemplate.opsForGeo().geoHash(key.getKey(), members);
    }

    @Override
    public List<Point> ggeoPos(Key key, V... members) {
        return redisTemplate.opsForGeo().geoPos(key.getKey(), members);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadius(Key key, Circle within) {
        return redisTemplate.opsForGeo().geoRadius(key.getKey(), within);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadius(Key key, Circle within, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().geoRadius(key.getKey(), within, args);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadiusByMember(Key key, V member, double radius) {
        return redisTemplate.opsForGeo().geoRadiusByMember(key.getKey(), member, radius);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadiusByMember(Key key, V member, Distance distance) {
        return redisTemplate.opsForGeo().geoRadiusByMember(key.getKey(), member, distance);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadiusByMember(
            Key key, V member, Distance distance, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().geoRadiusByMember(key.getKey(), member, distance, args);
    }

    @Override
    public Long ggeoRemove(Key key, V... members) {
        return redisTemplate.opsForGeo().geoRemove(key.getKey(), members);
    }

    @Override
    public Long gadd(Key key, Point point, V member) {
        return redisTemplate.opsForGeo().add(key.getKey(), point, member);
    }

    @Override
    public Long gadd(Key key, RedisGeoCommands.GeoLocation<V> location) {
        return redisTemplate.opsForGeo().add(key.getKey(), location);
    }

    @Override
    public Long gadd(Key key, Map<V, Point> memberCoordinateMap) {
        return redisTemplate.opsForGeo().add(key.getKey(), memberCoordinateMap);
    }

    @Override
    public Long gadd(Key key, Iterable<RedisGeoCommands.GeoLocation<V>> geoLocations) {
        return redisTemplate.opsForGeo().add(key.getKey(), geoLocations);
    }

    @Override
    public Distance gdistance(Key key, V member1, V member2) {
        return redisTemplate.opsForGeo().distance(key.getKey(), member1, member2);
    }

    @Override
    public Distance gdistance(Key key, V member1, V member2, Metric metric) {
        return redisTemplate.opsForGeo().distance(key.getKey(), member1, member2, metric);
    }

    @Override
    public List<String> ghash(Key key, V... members) {
        return redisTemplate.opsForGeo().hash(key.getKey(), members);
    }

    @Override
    public List<Point> gposition(Key key, V... members) {
        return redisTemplate.opsForGeo().position(key.getKey(), members);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(Key key, Circle within) {
        return redisTemplate.opsForGeo().radius(key.getKey(), within);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(Key key, Circle within, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().radius(key.getKey(), within, args);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(Key key, V member, double radius) {
        return redisTemplate.opsForGeo().radius(key.getKey(), member, radius);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(Key key, V member, Distance distance) {
        return redisTemplate.opsForGeo().radius(key.getKey(), member, distance);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(Key key, V member, Distance distance, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().radius(key.getKey(), member, distance, args);
    }

    @Override
    public Long gremove(Key key, V... members) {
        return redisTemplate.opsForGeo().remove(key.getKey(), members);
    }

    /**
     * zset
     */


    @Override
    public Long zunionAndStore(Key key, Collection otherKeys, Key destKey, RedisZSetCommands.Aggregate aggregate) {
        return redisTemplate.opsForZSet().unionAndStore(key.getKey(), otherKeys, destKey.getKey(), aggregate);
    }

    @Override
    public Long zintersectAndStore(Key key, Collection otherKeys, Key destKey, RedisZSetCommands.Aggregate aggregate) {
        return redisTemplate.opsForZSet().intersectAndStore(key.getKey(), otherKeys, destKey.getKey(), aggregate);
    }

    @Override
    public Boolean zadd(Key key, V value, double score) {
        return redisTemplate.opsForZSet().add(key.getKey(), value, score);
    }

    @Override
    public Long zadd(Key key, Set set) {
        return redisTemplate.opsForZSet().add(key.getKey(), set);
    }

    @Override
    public Long zremove(Key key, V... values) {
        return redisTemplate.opsForZSet().remove(key.getKey(), values);
    }

    @Override
    public Double zincrementScore(Key key, V value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key.getKey(), value, delta);
    }

    @Override
    public Long zrank(Key key, Object o) {
        return redisTemplate.opsForZSet().rank(key.getKey(), o);
    }

    @Override
    public Long zreverseRank(Key key, Object o) {
        return redisTemplate.opsForZSet().reverseRank(key.getKey(), o);
    }

    @Override
    public Set<V> zrange(Key key, long start, long end) {
        return redisTemplate.opsForZSet().range(key.getKey(), start, end);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<V>> zrangeWithScores(Key key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key.getKey(), start, end);
    }

    @Override
    public Set<V> zrangeByScore(Key key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key.getKey(), min, max);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<V>> zrangeByScoreWithScores(Key key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key.getKey(), min, max);
    }

    @Override
    public Set<V> zrangeByScore(Key key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScore(key.getKey(), min, max, offset, count);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<V>> zrangeByScoreWithScores(Key key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key.getKey(), min, max, offset, count);
    }

    @Override
    public Set<V> zreverseRange(Key key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key.getKey(), start, end);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<V>> zreverseRangeWithScores(Key key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key.getKey(), start, end);
    }

    @Override
    public Set<V> zreverseRangeByScore(Key key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key.getKey(), min, max);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<V>> zreverseRangeByScoreWithScores(Key key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key.getKey(), min, max);
    }

    @Override
    public Set<V> zreverseRangeByScore(Key key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key.getKey(), min, max, offset, count);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<V>> zreverseRangeByScoreWithScores(Key key, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key.getKey(), min, max, offset, count);
    }

    @Override
    public Long zcount(Key key, double min, double max) {
        return redisTemplate.opsForZSet().count(key.getKey(), min, max);
    }

    @Override
    public Long zsize(Key key) {
        return redisTemplate.opsForZSet().size(key.getKey());
    }

    @Override
    public Long zCard(Key key) {
        return redisTemplate.opsForZSet().zCard(key.getKey());
    }

    @Override
    public Double zscore(Key key, Object o) {
        return redisTemplate.opsForZSet().score(key.getKey(), o);
    }

    @Override
    public Long zremoveRange(Key key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key.getKey(), start, end);
    }

    @Override
    public Long zremoveRangeByScore(Key key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key.getKey(), min, max);
    }

    @Override
    public Long zunionAndStore(Key key, Key otherKey, Key destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key.getKey(), otherKey.getKey(), destKey.getKey());
    }

    @Override
    public Long zunionAndStore(Key key, Collection otherKeys, Key destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key.getKey(), otherKeys, destKey.getKey());
    }

    @Override
    public Long zunionAndStore(Key key, Collection otherKeys, Key destKey, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights) {
        return redisTemplate.opsForZSet().unionAndStore(key.getKey(), otherKeys, destKey.getKey(), aggregate, weights);
    }

    @Override
    public Long zintersectAndStore(Key key, Key otherKey, Key destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key.getKey(), otherKey.getKey(), destKey.getKey());
    }

    @Override
    public Long zintersectAndStore(Key key, Collection otherKeys, Key destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key.getKey(), otherKeys, destKey.getKey());
    }

    @Override
    public Long zintersectAndStore(
            Key key, Collection otherKeys, Key destKey, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights) {
        return redisTemplate.opsForZSet().intersectAndStore(key.getKey(), otherKeys, destKey.getKey(), aggregate, weights);
    }

    @Override
    public Cursor<ZSetOperations.TypedTuple<V>> zscan(Key key, ScanOptions options) {
        return redisTemplate.opsForZSet().scan(key.getKey(), options);
    }

    @Override
    public Set<V> zrangeByLex(Key key, RedisZSetCommands.Range range) {
        return redisTemplate.opsForZSet().rangeByLex(key.getKey(), range);
    }

    @Override
    public Set<V> zrangeByLex(Key key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
        return redisTemplate.opsForZSet().rangeByLex(key.getKey(), range, limit);
    }

    @Override
    public RedisOperations zgetOperations() {
        return redisTemplate.opsForZSet().getOperations();
    }

    /*
     ValueOperations
     */
    /**
     * set命令
     * @param key  key
     * @param value value
     * @param timeout 过期时间
     */
    @Override
    public void set(Key key, V value, Duration timeout) {
        redisTemplate.opsForValue().set(key.getKey(), value, timeout);
    }

    /**
     * setIfAbsent命令
     * @param key  key
     * @param value value
     * @param timeout 过期时间
     */
    @Override
    public Boolean setIfAbsent(Key key, V value, Duration timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key.getKey(), value, timeout);
    }

    @Override
    public Boolean setIfPresent(Key key, V value, Duration timeout) {
        return redisTemplate.opsForValue().setIfPresent(key.getKey(), value, timeout);
    }

    /**
     * set命令
     * @param key  key
     * @param value value
     */
    @Override
    public void set(Key key, V value) {
        redisTemplate.opsForValue().set(key.getKey(), value);
    }



    /**
     * set命令
     * @param key  key
     * @param value value
     * @param seconds 过期时间
     */
    @Override
    public void set(Key key, V value, long seconds) {
        redisTemplate.opsForValue().set(key.getKey(), value, seconds, TimeUnit.SECONDS);
    }

    /**
     * set命令
     * @param key  key
     * @param value value
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    @Override
    public void set(Key key, V value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key.getKey(), value, timeout, unit);
    }

    /**
     * setIfAbsent命令
     * @param key  key
     * @param value value
     */
    @Override
    public Boolean setIfAbsent(Key key, V value) {
        return redisTemplate.opsForValue().setIfAbsent(key.getKey(), value);
    }

    /**
     * setIfAbsent命令
     * @param key  key
     * @param value value
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    @Override
    public Boolean setIfAbsent(Key key, V value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key.getKey(), value, timeout, unit);
    }

    /**
     * setIfAbsent命令
     * @param key  key
     * @param value value
     */
    @Override
    public Boolean setIfPresent(Key key, V value) {
        return redisTemplate.opsForValue().setIfPresent(key.getKey(), value);
    }

    /**
     * setIfAbsent命令
     * @param key  key
     * @param value value
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    @Override
    public Boolean setIfPresent(Key key, V value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfPresent(key.getKey(), value, timeout, unit);
    }

    /**
     * setIfAbsent命令
     * @param key  key
     * @param value value
     * @param seconds 过期时间
     */
    @Override
    public Boolean setIfPresent(Key key, V value, long seconds) {
        return redisTemplate.opsForValue().setIfPresent(key.getKey(), value, seconds, TimeUnit.SECONDS);
    }

    /**
     * multiSet命令
     * @param map
     */
    @Override
    public void multiSet(Map<Key, V> map) {
        Objects.requireNonNull(map, "map");
        if (map.isEmpty()) {
            throw new TycRedisException("map is empty");
        }
        Map<String, V> newMap = new HashMap<>();
        for (Map.Entry<Key, V> entry : map.entrySet()) {
            newMap.put(entry.getKey().getKey(), entry.getValue());
        }
        redisTemplate.opsForValue().multiSet(newMap);
    }

    /**
     * 批量设置键值对，并设置键的过期时间
     *
     * @param map      键值对映射
     * @param expireMS 过期时间（毫秒）
     * @return
     */
    public void multiSetExpire(Map<Key, String> map, Integer expireMS) {
        Objects.requireNonNull(map, "map");
        Objects.requireNonNull(expireMS, "expireMS");
        if (map.isEmpty() || expireMS <= 0) {
            throw new TycRedisException("map is empty or expireMS <= 0");
        }
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConnection = (StringRedisConnection) connection;
            for (Map.Entry<Key, String> entry : map.entrySet()) {
                try {
                    stringRedisConnection.set(entry.getKey().getKey(), entry.getValue(), Expiration.milliseconds(expireMS),
                            RedisStringCommands.SetOption.UPSERT);
                } catch (Throwable e) {
                    throw new TycRedisException("multiSetExpire", e);
                }
            }
            return null;
        });
    }

    /**
     * multiSet命令
     * @param map
     */
    @Override
    public Boolean multiSetIfAbsent(Map<Key, V> map) {
        Map<String, V> newMap = new HashMap<>();
        for (Map.Entry<Key, V> keyVEntry : map.entrySet()) {
            newMap.put(keyVEntry.getKey().getKey(), keyVEntry.getValue());
        }
        return redisTemplate.opsForValue().multiSetIfAbsent(newMap);
    }

    /**
     * get命令
     * @param key
     */
    @Override
    public V get(Key key) {
        return redisTemplate.opsForValue().get(key.getKey());
    }

    @Override
    public V getAndSet(Key key, V value) {
        return redisTemplate.opsForValue().getAndSet(key.getKey(), value);
    }

    @Override
    public List<V> multiGet(Collection<Key> keys) {
        return redisTemplate.opsForValue().multiGet(toListKey(keys));
    }

    /**
     * 数字初始化命令, 使用StringRedisTemplate
     * @param key  key
     * @param number value
     */
    public void initNumber(Key key, long number) {
        stringRedisTemplate.opsForValue().set(key.getKey(), String.valueOf(number));
    }

    /**
     * 数字初始化命令, 使用StringRedisTemplate
     * @param key  key
     * @param number value
     */
    public void initNumber(Key key, double number) {
        stringRedisTemplate.opsForValue().set(key.getKey(), String.valueOf(number));
    }

    @Override
    public Long increment(Key key) {
        return stringRedisTemplate.opsForValue().increment(key.getKey());
    }

    @Override
    public Long increment(Key key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key.getKey(), delta);
    }

    @Override
    public Double increment(Key key, double delta) {
        return stringRedisTemplate.opsForValue().increment(key.getKey(), delta);
    }

    @Override
    public Long decrement(Key key) {
        return stringRedisTemplate.opsForValue().decrement(key.getKey());
    }

    @Override
    public Long decrement(Key key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key.getKey(), delta);
    }

    @Override
    public Integer append(Key key, String value) {
        return redisTemplate.opsForValue().append(key.getKey(), value);
    }

    @Override
    public String get(Key key, long start, long end) {
        return redisTemplate.opsForValue().get(key.getKey(), start, end);
    }

    @Override
    public Long size(Key key) {
        return redisTemplate.opsForValue().size(key.getKey());
    }

    @Override
    public Boolean setBit(Key key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key.getKey(), offset, value);
    }

    @Override
    public Boolean getBit(Key key, long offset) {
        return redisTemplate.opsForValue().getBit(key.getKey(), offset);
    }

    @Override
    public List<Long> bitField(Key key, BitFieldSubCommands subCommands) {
        return redisTemplate.opsForValue().bitField(key.getKey(), subCommands);
    }

    @Override
    public RedisOperations getOperations() {
        return redisTemplate.opsForValue().getOperations();
    }

    /**
     * set
     */
    @Override
    public Long sadd(Key key, V[] values) {
        return redisTemplate.opsForSet().add(key.getKey(), values);
    }

    @Override
    public Long sremove(Key key, V... values) {
        return redisTemplate.opsForSet().remove(key.getKey(), values);
    }

    @Override
    public V spop(Key key) {
        return redisTemplate.opsForSet().pop(key.getKey());
    }

    @Override
    public List<V> spop(Key key, long count) {
        return redisTemplate.opsForSet().pop(key.getKey(), count);
    }

    @Override
    public Boolean smove(Key key, V value, Key destKey) {
        return redisTemplate.opsForSet().move(key.getKey(), value, destKey.getKey());
    }

    @Override
    public Long ssize(Key key) {
        return redisTemplate.opsForSet().size(key.getKey());
    }

    @Override
    public Boolean sisMember(Key key, Object o) {
        return redisTemplate.opsForSet().isMember(key.getKey(), o);
    }

    @Override
    public Set<V> sintersect(Key key, Key otherKey) {
        return redisTemplate.opsForSet().intersect(key.getKey(), otherKey.getKey());
    }

    @Override
    public Set<V> sintersect(Key key, Collection otherKeys) {
        return redisTemplate.opsForSet().intersect(key.getKey(), toListKey(otherKeys));
    }

    @Override
    public Set<V> sintersect(Collection keys) {
        return redisTemplate.opsForSet().intersect(toListKey(keys));
    }

    @Override
    public Long sintersectAndStore(Key key, Key otherKey, Key destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key.getKey(), otherKey.getKey(), destKey.getKey());
    }

    @Override
    public Long sintersectAndStore(Key key, Collection otherKeys, Key destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key.getKey(), toListKey(otherKeys), destKey.getKey());
    }

    @Override
    public Long sintersectAndStore(Collection keys, Key destKey) {
        return redisTemplate.opsForSet().intersectAndStore(toListKey(keys), destKey.getKey());
    }

    @Override
    public Set sunion(Key key, Key otherKey) {
        return redisTemplate.opsForSet().union(key.getKey(), otherKey.getKey());
    }

    @Override
    public Set<V> sunion(Key key, Collection otherKeys) {
        return redisTemplate.opsForSet().union(key.getKey(), toListKey(otherKeys));
    }

    @Override
    public Set<V> sunion(Collection keys) {
        return redisTemplate.opsForSet().union(toListKey(keys));
    }

    @Override
    public Long sunionAndStore(Key key, Key otherKey, Key destKey) {
        return redisTemplate.opsForSet().unionAndStore(key.getKey(), otherKey.getKey(), destKey.getKey());
    }

    @Override
    public Long sunionAndStore(Key key, Collection otherKeys, Key destKey) {
        return redisTemplate.opsForSet().unionAndStore(key.getKey(), toListKey(otherKeys), destKey.getKey());
    }

    @Override
    public Long sunionAndStore(Collection keys, Key destKey) {
        return redisTemplate.opsForSet().unionAndStore(toListKey(keys), destKey.getKey());
    }

    @Override
    public Set<V> sdifference(Key key, Key otherKey) {
        return redisTemplate.opsForSet().difference(key.getKey(), otherKey.getKey());
    }

    @Override
    public Set<V> sdifference(Key key, Collection otherKeys) {
        return redisTemplate.opsForSet().difference(key.getKey(), toListKey(otherKeys));
    }

    @Override
    public Set<V> sdifference(Collection keys) {
        return redisTemplate.opsForSet().difference(toListKey(keys));
    }

    @Override
    public Long sdifferenceAndStore(Key key, Key otherKey, Key destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key.getKey(), otherKey.getKey(), destKey.getKey());
    }

    @Override
    public Long sdifferenceAndStore(Key key, Collection otherKeys, Key destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key.getKey(), toListKey(otherKeys), destKey.getKey());
    }

    @Override
    public Long sdifferenceAndStore(Collection keys, Key destKey) {
        return redisTemplate.opsForSet().differenceAndStore(toListKey(keys), destKey.getKey());
    }

    @Override
    public Set<V> smembers(Key key) {
        return redisTemplate.opsForSet().members(key.getKey());
    }

    @Override
    public V srandomMember(Key key) {
        return redisTemplate.opsForSet().randomMember(key.getKey());
    }

    @Override
    public Set sdistinctRandomMembers(Key key, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key.getKey(), count);
    }

    @Override
    public List<V> srandomMembers(Key key, long count) {
        return redisTemplate.opsForSet().randomMembers(key.getKey(), count);
    }

    @Override
    public Cursor<V> sscan(Key key, ScanOptions options) {
        return redisTemplate.opsForSet().scan(key.getKey(), options);
    }

    @Override
    public RedisOperations<String, V> sgetOperations() {
        return redisTemplate.opsForSet().getOperations();
    }

    /**
     * list
     */
    @Override
    public V lleftPop(Key key, Duration timeout) {
        return redisTemplate.opsForList().leftPop(key.getKey(), timeout);
    }

    @Override
    public V lrightPop(Key key, Duration timeout) {
        return redisTemplate.opsForList().rightPop(key.getKey(), timeout);
    }

    @Override
    public V lrightPopAndLeftPush(Key sourceKey, Key key, Duration timeout) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey.getKey(), key.getKey(), timeout);
    }

    @Override
    public List<V> lrange(Key key, long start, long end) {
        return redisTemplate.opsForList().range(key.getKey(), start, end);
    }

    @Override
    public void ltrim(Key key, long start, long end) {
        redisTemplate.opsForList().trim(key.getKey(), start, end);
    }

    @Override
    public Long lsize(Key key) {
        return redisTemplate.opsForList().size(key.getKey());
    }

    @Override
    public Long lleftPush(Key key, V value) {
        return redisTemplate.opsForList().leftPush(key.getKey(), value);
    }

    @Override
    public Long lleftPushAll(Key key, V... values) {
        return redisTemplate.opsForList().leftPushAll(key.getKey(), values);
    }

    @Override
    public Long lleftPushAll(Key key, Collection<V> values) {
        return redisTemplate.opsForList().leftPushAll(key.getKey(), values);
    }

    @Override
    public Long lleftPushIfPresent(Key key, V value) {
        return redisTemplate.opsForList().leftPushIfPresent(key.getKey(), value);
    }

    @Override
    public Long lleftPush(Key key, V pivot, V value) {
        return redisTemplate.opsForList().leftPush(key.getKey(), pivot, value);
    }

    @Override
    public Long lrightPush(Key key, V value) {
        return redisTemplate.opsForList().rightPush(key.getKey(), value);
    }

    @Override
    public Long lrightPushAll(Key key, V... values) {
        return redisTemplate.opsForList().rightPushAll(key.getKey(), values);
    }

    @Override
    public Long lrightPushAll(Key key, Collection<V> values) {
        return redisTemplate.opsForList().rightPushAll(key.getKey(), values);
    }

    @Override
    public Long lrightPushIfPresent(Key key, V value) {
        return redisTemplate.opsForList().rightPushIfPresent(key.getKey(), value);
    }

    @Override
    public Long lrightPush(Key key, V pivot, V value) {
        return redisTemplate.opsForList().rightPush(key.getKey(), pivot, value);
    }

    @Override
    public void lset(Key key, long index, V value) {
        redisTemplate.opsForList().set(key.getKey(), index, value);
    }

    @Override
    public Long lremove(Key key, long count, V value) {
        return redisTemplate.opsForList().remove(key.getKey(), count, value);
    }

    @Override
    public V lindex(Key key, long index) {
        return redisTemplate.opsForList().index(key.getKey(), index);
    }

    @Override
    public V lleftPop(Key key) {
        return redisTemplate.opsForList().leftPop(key.getKey());
    }

    @Override
    public V lleftPop(Key key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key.getKey(), timeout, unit);
    }

    @Override
    public V lrightPop(Key key) {
        return redisTemplate.opsForList().rightPop(key.getKey());
    }

    @Override
    public V lrightPop(Key key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key.getKey(), timeout, unit);
    }

    @Override
    public V lrightPopAndLeftPush(Key sourceKey, Key destinationKey) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey.getKey(), destinationKey.getKey());
    }

    @Override
    public V lrightPopAndLeftPush(Key sourceKey, Key destinationKey, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey.getKey(), destinationKey.getKey(), timeout, unit);
    }

    @Override
    public RedisOperations<String, V> lgetOperations() {
        return redisTemplate.opsForList().getOperations();
    }

    /**
     * hypperloglog
     */

    @Override
    public Long hyperadd(Key key, V... values) {
        return redisTemplate.opsForHyperLogLog().add(key.getKey(), values);
    }

    @Override
    public Long hypersize(Key... keys) {
        return redisTemplate.opsForHyperLogLog().size(toListKey(keys));
    }

    @Override
    public Long hyperunion(Key destination, Key... sourceKeys) {
        return redisTemplate.opsForHyperLogLog().union(destination.getKey(), toListKey(sourceKeys));
    }

    @Override
    public void hyperdelete(Key key) {
        redisTemplate.opsForHyperLogLog().delete(key.getKey());
    }

    /**
     * hash
     */

    @Override
    public Long hdelete(Key key, String... hashKeys) {
        return redisTemplate.opsForHash().delete(key.getKey(), hashKeys);
    }

    @Override
    public Boolean hhasKey(Key key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key.getKey(), hashKey);
    }

    @Override
    public V hget(Key key, String hashKey) {
        return redisTemplate.<String, V>opsForHash().get(key.getKey(), hashKey);
    }


    @Override
    public List<V> hmultiGet(Key key, Collection<String> hashKeys) {
        if (CollectionUtils.isEmpty(hashKeys)) {
            return new ArrayList<>();
        }

        return redisTemplate.<String, V>opsForHash().multiGet(key.getKey(), hashKeys);
    }

    @Override
    public Long hincrement(Key key, String hashKey, long delta) {
        return redisTemplate.<String, V>opsForHash().increment(key.getKey(), hashKey, delta);
    }

    @Override
    public Double hincrement(Key key, String hashKey, double delta) {
        return redisTemplate.<String, V>opsForHash().increment(key.getKey(), hashKey, delta);
    }

    @Override
    public Long hlengthOfValue(Key key, String hashKey) {
        return redisTemplate.opsForHash().lengthOfValue(key.getKey(), hashKey);
    }

    @Override
    public Long hsize(Key key) {
        return redisTemplate.opsForHash().size(key.getKey());
    }

    @Override
    public void hputAll(Key key, Map<String, V> map) {
        redisTemplate.opsForHash().putAll(key.getKey(), map);
    }

    @Override
    public void hput(Key key, String hashKey, V value) {
        redisTemplate.<String, V>opsForHash().put(key.getKey(), hashKey, value);
    }

    @Override
    public Boolean hputIfAbsent(Key key, String hashKey, V value) {
        return redisTemplate.<String, V>opsForHash().putIfAbsent(key.getKey(), hashKey, value);
    }

    @Override
    public List<V> hvalues(Key key) {
        return redisTemplate.<String, V>opsForHash().values(key.getKey());
    }

    @Override
    public Map<String, V> hentries(Key key) {
        return redisTemplate.<String, V>opsForHash().entries(key.getKey());
    }

    @Override
    public Cursor<Map.Entry<String, V>> hscan(Key key, ScanOptions options) {
        return redisTemplate.<String, V>opsForHash().scan(key.getKey(), options);
    }

    @Override
    public RedisOperations<String, ?> hgetOperations() {
        return redisTemplate.<String, V>opsForHash().getOperations();
    }

    @Override
    public <T> T execute(RedisCallback<T> action) {
        return redisTemplate.execute(action);
    }

    @Override
    public <T> T execute(RedisCallback<T> action, boolean exposeConnection) {
        return redisTemplate.execute(action, exposeConnection);
    }

    @Override
    public <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline) {
        return redisTemplate.execute(action, exposeConnection, pipeline);
    }

    @Override
    public <T> T execute(SessionCallback<T> session) {
        return redisTemplate.execute(session);
    }

    @Override
    public List<Object> executePipelined(SessionCallback<?> session) {
        return redisTemplate.executePipelined(session);
    }

    @Override
    public List<Object> executePipelined(SessionCallback<?> session, RedisSerializer<?> resultSerializer) {
        return redisTemplate.executePipelined(session, resultSerializer);
    }

    @Override
    public List<Object> executePipelined(RedisCallback<?> action) {
        return redisTemplate.executePipelined(action);
    }

    @Override
    public List<Object> executePipelined(RedisCallback<?> action, RedisSerializer<?> resultSerializer) {
        return redisTemplate.executePipelined(action, resultSerializer);
    }

    @Override
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }

    @Override
    public <T extends Closeable> T executeWithStickyConnection(RedisCallback<T> callback) {
        return redisTemplate.executeWithStickyConnection(callback);
    }

    @Override
    public Boolean delete(Key key) {
        return redisTemplate.delete(key.getKey());
    }

    @Override
    public Long delete(Collection<Key> keys) {
        return redisTemplate.delete(toListKey(keys));
    }

    @Override
    public Boolean hasKey(Key key) {
        return redisTemplate.hasKey(key.getKey());
    }

    @Override
    public Long countExistingKeys(Collection<Key> keys) {
        return redisTemplate.countExistingKeys(toListKey(keys));
    }

    @Override
    public Boolean expire(Key key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key.getKey(), timeout, unit);
    }

    @Override
    public Boolean expireAt(Key key, Date date) {
        return redisTemplate.expireAt(key.getKey(), date);
    }

    @Override
    public Long getExpire(Key key) {
        return redisTemplate.getExpire(key.getKey());
    }

    @Override
    public Long getExpire(Key key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key.getKey(), timeUnit);
    }

    @Override
    public Boolean persist(Key key) {
        return redisTemplate.persist(key.getKey());
    }

    @Override
    public Boolean move(Key key, int dbIndex) {
        return redisTemplate.move(key.getKey(), dbIndex);
    }

    @Override
    public String randomKey() {
        return redisTemplate.randomKey();
    }

    @Override
    public void rename(Key oldKey, Key key) {
        redisTemplate.rename(oldKey.getKey(), key.getKey());
    }

    @Override
    public Boolean renameIfAbsent(Key oldKey, Key key) {
        return redisTemplate.renameIfAbsent(oldKey.getKey(), key.getKey());
    }

    @Override
    public DataType type(Key key) {
        return redisTemplate.type(key.getKey());
    }

    @Override
    public byte[] dump(Key key) {
        return redisTemplate.dump(key.getKey());
    }

    @Override
    public Boolean expire(Key key, Duration timeout) {
        return redisTemplate.expire(key.getKey(), timeout);
    }

    @Override
    public Boolean expireAt(Key key, Instant expireAt) {
        return redisTemplate.expireAt(key.getKey(), expireAt);
    }
}
