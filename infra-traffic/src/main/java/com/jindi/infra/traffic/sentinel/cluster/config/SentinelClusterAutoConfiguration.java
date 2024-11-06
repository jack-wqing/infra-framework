package com.jindi.infra.traffic.sentinel.cluster.config;

import com.jindi.infra.traffic.sentinel.cluster.properties.SentinelClusterProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.traffic.sentinel.cluster.init.SentinelClusterClientInitializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(SentinelClusterProperties.class)
@ConditionalOnProperty(name = {"rpc.sentinel.cluster.enable"}, havingValue = "true")
public class SentinelClusterAutoConfiguration {

    @Autowired
    private SentinelClusterProperties properties;

    @ConditionalOnMissingBean
    @Bean
    public SentinelClusterClientInitializer sentinelClusterClientInitializer() {
        return new SentinelClusterClientInitializer(properties);
    }

}
