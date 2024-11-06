package com.jindi.infra.cache.redis.client;


import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

public class TycRedisClient<V> extends BaseTycRedisClient<V> {

    private TycRedisHolder tycRedisHolder;

    public TycRedisClient() {
    }

    public TycRedisClient(RedisTemplate<String, V> redisTemplate, StringRedisTemplate stringRedisTemplate, TycRedisHolder tycRedisHolder) {
        super(redisTemplate, stringRedisTemplate);
        this.tycRedisHolder = tycRedisHolder;
    }

    public TycRedisHolder getTycRedisHolder() {
        return tycRedisHolder;
    }

    public void setTycRedisHolder(TycRedisHolder tycRedisHolder) {
        this.tycRedisHolder = tycRedisHolder;
    }
}
