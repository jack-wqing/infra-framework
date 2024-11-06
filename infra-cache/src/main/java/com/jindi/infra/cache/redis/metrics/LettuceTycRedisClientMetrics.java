package com.jindi.infra.cache.redis.metrics;


import com.jindi.infra.cache.redis.utils.RedisConnectInfoUtils;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;

@Slf4j
public class LettuceTycRedisClientMetrics extends BaseTycRedisClientMetrics {

    public LettuceTycRedisClientMetrics() {
        this(Collections.emptyList());
    }

    public LettuceTycRedisClientMetrics(Iterable<Tag> tags) {
        super(tags);
    }

    @Override
    public String getHostName(RedisTemplate redisTemplate) {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (!(connectionFactory instanceof LettuceConnectionFactory)) {
            return StringUtils.EMPTY;
        }
        LettuceConnectionFactory factory = (LettuceConnectionFactory) connectionFactory;
        return factory.getHostName();
    }

    @Override
    public String getPort(RedisTemplate redisTemplate) {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (!(connectionFactory instanceof LettuceConnectionFactory)) {
            return StringUtils.EMPTY;
        }
        LettuceConnectionFactory factory = (LettuceConnectionFactory) connectionFactory;
        return String.valueOf(factory.getPort());
    }

    @Override
    public String getDatabase(RedisTemplate redisTemplate) {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (!(connectionFactory instanceof LettuceConnectionFactory)) {
            return StringUtils.EMPTY;
        }
        LettuceConnectionFactory factory = (LettuceConnectionFactory) connectionFactory;
        return String.valueOf(factory.getDatabase());
    }

    @Override
    public String getConnection(RedisTemplate redisTemplate) {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (!(connectionFactory instanceof LettuceConnectionFactory)) {
            return StringUtils.EMPTY;
        }
        LettuceConnectionFactory factory = (LettuceConnectionFactory) connectionFactory;
        return RedisConnectInfoUtils.getConnectInfo(factory.getHostName(), factory.getPort(), factory.getDatabase());
    }
}
