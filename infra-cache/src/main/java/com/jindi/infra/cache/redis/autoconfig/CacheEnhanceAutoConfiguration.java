package com.jindi.infra.cache.redis.autoconfig;

import com.jindi.infra.cache.redis.enhance.RedisExtendCommand;
import com.jindi.infra.cache.redis.lock.DistributedLockService;
import com.jindi.infra.cache.redis.lock.aspect.DistributedLockAspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfigureAfter(RedisAutoConfiguration.class)
@Import(RedisAutoConfiguration.class)
@ConditionalOnSingleCandidate(StringRedisTemplate.class)
@Configuration
@EnableAspectJAutoProxy
public class CacheEnhanceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockService tycDistributedLockService(StringRedisTemplate stringRedisTemplate) {
        return new DistributedLockService(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect tycDistributedLockAspect(DistributedLockService distributedLockService) {
        return new DistributedLockAspect(distributedLockService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisExtendCommand redisExtendCommand(StringRedisTemplate stringRedisTemplate) {
        return new RedisExtendCommand(stringRedisTemplate);
    }
}
