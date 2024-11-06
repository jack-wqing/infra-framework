package com.jindi.infra.traffic.sentinel.cluster.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.jindi.infra.traffic.sentinel.cluster.discovery.NacosTokenServerDiscovery;
import com.jindi.infra.traffic.sentinel.cluster.selector.HashSelector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"rpc.sentinel.cluster.enable"}, havingValue = "true")
@ConditionalOnClass({NacosNamingService.class, NacosNamingMaintainService.class, ConfigService.class})
public class SentinelClusterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosTokenServerDiscovery nacosTokenServerDiscovery() {
        return new NacosTokenServerDiscovery();
    }

    @Bean
    @ConditionalOnMissingBean
    public HashSelector hashSelector() {
        return new HashSelector();
    }

}
