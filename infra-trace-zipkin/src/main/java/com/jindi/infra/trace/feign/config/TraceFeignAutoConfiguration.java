package com.jindi.infra.trace.feign.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.trace.feign.processor.InfraFeignContextBeanPostProcessor;

import feign.Client;

@Configuration
@ConditionalOnProperty(value = "trace.feign.enabled", matchIfMissing = true)
@ConditionalOnClass({ Client.class, FeignContext.class })
@AutoConfigureBefore({ FeignAutoConfiguration.class, FeignContext.class })
public class TraceFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InfraFeignContextBeanPostProcessor infraFeignContextBeanPostProcessor() {
        return new InfraFeignContextBeanPostProcessor();
    }
}
