package com.jindi.infra.cache.redis.enhance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class RedisExtendCommand {

    private StringRedisTemplate stringRedisTemplate;

    public RedisExtendCommand(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 批量设置键值对，并设置键的过期时间
     *
     * @param map      键值对映射
     * @param expireMS 过期时间（毫秒）
     * @return
     */
    public void multiSetExpire(Map<String, String> map, Integer expireMS) {
        Objects.requireNonNull(map, "map");
        Objects.requireNonNull(expireMS, "expireMS");
        if (map.isEmpty() || expireMS <= 0) {
            log.error("map is empty or expireMS <= 0");
            return;
        }
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConnection = (StringRedisConnection) connection;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    stringRedisConnection.set(entry.getKey(), entry.getValue(), Expiration.milliseconds(expireMS),
                        RedisStringCommands.SetOption.UPSERT);
                } catch (Throwable e) {
                    log.error("multiSetExpire", e);
                }
            }
            return null;
        });
    }

    public void multiSet(Map<String, String> map) {
        stringRedisTemplate.opsForValue().multiSet(map);
    }
}
