package com.jindi.infra.cache.redis.autoconfig;


import com.jindi.infra.cache.redis.connectionfactory.BaseRedisConnectionFactory;
import com.jindi.infra.cache.redis.connectionfactory.TycRedisConnectionFactory;
import com.jindi.infra.cache.redis.connectionfactory.jedis.JedisRedisConnectionFactory;
import com.jindi.infra.cache.redis.connectionfactory.lettuce.LettuceRedisConnectionFactory;
import com.jindi.infra.cache.redis.interceptor.CatRedisInterceptor;
import com.jindi.infra.cache.redis.interceptor.RedisConnectRebuildInterceptor;
import com.jindi.infra.cache.redis.interceptor.RedisOperateAspect;
import com.jindi.infra.cache.redis.metrics.JedisTycRedisClientMetrics;
import com.jindi.infra.cache.redis.metrics.LettuceTycRedisClientMetrics;
import com.jindi.infra.cache.redis.processor.TycRedisPostProcessor;
import com.jindi.infra.cache.redis.processor.TycRedisPropertiesPostProcessor;
import io.lettuce.core.RedisClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import redis.clients.jedis.Jedis;

import java.util.List;

@Configuration
@Import({TycRedisAutoConfiguration.JedisConfiguration.class, TycRedisAutoConfiguration.LettuceConfiguration.class})
public class TycRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TycRedisConnectionFactory tycRedisConnectionFactory(List<BaseRedisConnectionFactory> iRedisConnectionFactories) {
        return new TycRedisConnectionFactory(iRedisConnectionFactories);
    }

    @Bean
    @ConditionalOnMissingBean
    public TycRedisPostProcessor tycRedisPostProcessor(TycRedisConnectionFactory tycRedisConnectionFactory) {
        return new TycRedisPostProcessor(tycRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public TycRedisPropertiesPostProcessor tycRedisPropertiesPostProcessor() {
        return new TycRedisPropertiesPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisOperateAspect redisOperateAspect() {
        return new RedisOperateAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public CatRedisInterceptor catRedisInterceptor() {
        return new CatRedisInterceptor();
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public RedisConnectRebuildInterceptor redisConnectRebuildInterceptor() {
//        return new RedisConnectRebuildInterceptor();
//    }

    @Configuration
    @ConditionalOnClass({RedisClient.class, GenericObjectPoolConfig.class})
    public static class LettuceConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public LettuceRedisConnectionFactory lettuceRedisIRedisConnectionFactory() {
            return new LettuceRedisConnectionFactory();
        }

        @Bean
        @ConditionalOnMissingBean
        public LettuceTycRedisClientMetrics lettuceTycRedisClientMetrics() {
            return new LettuceTycRedisClientMetrics();
        }
    }

    @Configuration
    @ConditionalOnClass({ GenericObjectPool.class, JedisConnection.class, Jedis.class })
    public static class JedisConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public JedisRedisConnectionFactory jedisRedisIRedisConnectionFactory() {
            return new JedisRedisConnectionFactory();
        }

        @Bean
        @ConditionalOnMissingBean
        public JedisTycRedisClientMetrics jedisTycRedisClientMetrics() {
            return new JedisTycRedisClientMetrics();
        }
    }

}
