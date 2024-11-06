package com.jindi.infra.cache.redis.client;


import com.jindi.infra.cache.redis.key.Key;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface IRedisTemplateClient<V> {

    Boolean delete(Key key);
    Long delete(Collection<Key> keys);
    Boolean hasKey(Key key);
    Long countExistingKeys(Collection<Key> keys);
    Boolean expire(Key key, long timeout, TimeUnit unit);
    Boolean expireAt(Key key, Date date);
    Long getExpire(Key key);
    Long getExpire(Key key, TimeUnit timeUnit);
    Boolean persist(Key key);
    Boolean move(Key key, int dbIndex);
    String randomKey();
    void rename(Key oldKey, Key newKey);
    Boolean renameIfAbsent(Key oldKey, Key newKey);
    DataType type(Key key);
    byte[] dump(Key key);
    Boolean expire(Key key, Duration timeout);
    Boolean expireAt(Key key, Instant expireAt);
}
