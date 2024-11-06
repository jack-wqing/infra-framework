package com.jindi.infra.registry;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.jindi.infra.registry.controller.NacosController;
import com.jindi.infra.registry.eureka.RemoveRefreshAllApplicationRunner;
import com.jindi.infra.registry.nacos.properties.NacosProperties;
import com.jindi.infra.registry.nacos.provider.HuaweiNacosDiscoveryProvider;
import com.jindi.infra.registry.nacos.provider.HuaweiServiceRegister;
import com.jindi.infra.registry.nacos.provider.NacosLoadBalancer;

@Configuration
@ConditionalOnProperty(name = "rpc.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(NacosProperties.class)
public class RegistryAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public NacosLoadBalancer nacosLoadBalancer() {
        return new NacosLoadBalancer();
    }

    @ConditionalOnMissingBean
    @Bean
    public HuaweiServiceRegister huaweiserviceRegister(@Qualifier("huaweiNamingService") NamingService namingService) {
        return new HuaweiServiceRegister(namingService);
    }

    @ConditionalOnMissingBean
    @Bean
    public HuaweiNacosDiscoveryProvider huaweiNacosDiscoveryProvider(NacosProperties nacosProperties,
                                                                     @Qualifier("huaweiNamingService") NamingService namingService, NacosLoadBalancer nacosLoadBalancer,
                                                                     HuaweiServiceRegister serviceRegister) {
        return new HuaweiNacosDiscoveryProvider(nacosProperties, namingService, nacosLoadBalancer, serviceRegister);
    }

    @ConditionalOnWebApplication
    @ConditionalOnMissingBean
    @Bean
    public NacosController nacosController() {
        return new NacosController();
    }

    @Bean("huaweiNamingService")
    public NamingService huaweiNamingService(NacosProperties nacosProperties) throws NacosException {
        return createNamingService(nacosProperties, nacosProperties.getServerAddr());
    }

    private NamingService createNamingService(NacosProperties nacosProperties, String serverAddr) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.USERNAME, nacosProperties.getUsername());
        properties.put(PropertyKeyConst.PASSWORD, nacosProperties.getPassword());
        if (StringUtils.isNotBlank(nacosProperties.getContextPath())) {
            properties.put(PropertyKeyConst.CONTEXT_PATH, nacosProperties.getContextPath());
        }
        if (StringUtils.isNotBlank(nacosProperties.getClusterName())) {
            properties.put(PropertyKeyConst.CLUSTER_NAME, nacosProperties.getClusterName());
        }
        if (StringUtils.isNotBlank(nacosProperties.getEndpoint())) {
            properties.put(PropertyKeyConst.ENDPOINT, nacosProperties.getEndpoint());
        }
        if (StringUtils.isNotBlank(nacosProperties.getNamespace())) {
            properties.put(PropertyKeyConst.NAMESPACE, nacosProperties.getNamespace());
        }
        if (StringUtils.isNotBlank(nacosProperties.getAccessKey())) {
            properties.put(PropertyKeyConst.ACCESS_KEY, nacosProperties.getAccessKey());
        }
        if (StringUtils.isNotBlank(nacosProperties.getSecretKey())) {
            properties.put(PropertyKeyConst.SECRET_KEY, nacosProperties.getSecretKey());
        }
        if (StringUtils.isNotBlank(nacosProperties.getNamingPushEmptyProtection())) {
            properties.put(PropertyKeyConst.NAMING_PUSH_EMPTY_PROTECTION, nacosProperties.getNamingPushEmptyProtection());
        }
        return NacosFactory.createNamingService(properties);
    }

    @ConditionalOnMissingBean
    @Bean
    public RemoveRefreshAllApplicationRunner removeRefreshAllApplicationRunner() {
        return new RemoveRefreshAllApplicationRunner();
    }
}
