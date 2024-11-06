package com.jindi.infra.cache.redis.lock;

import com.jindi.infra.cache.redis.autoconfig.CacheEnhanceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CacheEnhanceAutoConfiguration.class)
public class LockConfig {

    @Bean
    public Greeter greeter() {
        return new Greeter();
    }
}
