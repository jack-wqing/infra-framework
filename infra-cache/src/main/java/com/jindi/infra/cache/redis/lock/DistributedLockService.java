package com.jindi.infra.cache.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

/**
 * 分布式锁实现
 */
@Slf4j
public class DistributedLockService {

    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private StringRedisTemplate stringRedisTemplate;

    public DistributedLockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 申请锁（阻塞）
     *
     * @param key           键值对
     * @param timeoutMillis 超时；单位：毫秒
     * @return
     */
    public Boolean lock(String key, Integer timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (!tryLock(key, timeoutMillis)) {
            if ((System.currentTimeMillis() - startTime > timeoutMillis)) {
                return Boolean.FALSE;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.debug("", e);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 尝试申请锁; 系统异常申请锁成功
     *
     * @param key           键值对
     * @param timeoutMillis 超时；单位：毫秒
     * @return
     */
    public Boolean tryLock(String key, Integer timeoutMillis) {
        return tryLock(key, timeoutMillis, true);
    }

    /**
     * 尝试申请锁
     *
     * @param key           键值对
     * @param timeoutMillis 超时；单位：毫秒
     * @param defaultResult 系统异常申请锁状态
     * @return
     */
    public Boolean tryLock(String key, Integer timeoutMillis, Boolean defaultResult) {
        try {
            return stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(Thread.currentThread().getId()),
                Duration.ofMillis(timeoutMillis));
        } catch (Throwable e) {
            log.error("tryLock", e);
        }
        return defaultResult;
    }

    /**
     * 释放锁
     *
     * @param key
     * @return
     */
    public Boolean unlock(String key) {
        try {
            String threadId = String.valueOf(Thread.currentThread().getId());
            Long result = stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_SCRIPT, Collections.singletonList(key),
                        Collections.singletonList(threadId));
                } else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_SCRIPT, Collections.singletonList(key),
                        Collections.singletonList(threadId));
                }
                return 0L;
            });
            return Objects.equals(result, 1L);
        } catch (Throwable e) {
            log.error("unlock", e);
        }
        return true;
    }
}
