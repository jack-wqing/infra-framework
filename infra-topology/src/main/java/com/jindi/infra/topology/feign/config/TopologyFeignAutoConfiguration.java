package com.jindi.infra.topology.feign.config;

import feign.Client;
import com.jindi.infra.topology.feign.processor.TopologyFeignContextBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ Client.class, FeignContext.class })
@AutoConfigureBefore({ FeignAutoConfiguration.class, FeignContext.class })
public class TopologyFeignAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public TopologyFeignContextBeanPostProcessor topologyFeignContextBeanPostProcessor() {
        return new TopologyFeignContextBeanPostProcessor();
    }

}
