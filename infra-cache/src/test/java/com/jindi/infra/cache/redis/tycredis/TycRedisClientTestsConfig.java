package com.jindi.infra.cache.redis.tycredis;

import com.jindi.infra.cache.redis.autoconfig.CacheEnhanceAutoConfiguration;
import com.jindi.infra.cache.redis.autoconfig.TycRedisAutoConfiguration;
import com.jindi.infra.cache.redis.enhance.RedisExtendCommand;
import com.jindi.infra.cache.redis.interceptor.RedisOperateAspect;
import com.jindi.infra.cache.redis.lock.Greeter;
import com.jindi.infra.cache.redis.lock.LockConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Import(TycRedisAutoConfiguration.class)
public class TycRedisClientTestsConfig {

    @Bean
    @ConditionalOnMissingBean
    public RedisExtendCommand redisExtendCommand(StringRedisTemplate stringRedisTemplate) {
        return new RedisExtendCommand(stringRedisTemplate);
    }

}
