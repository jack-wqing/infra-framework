package com.jindi.infra.dataapi.oneservice.config;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts;
import com.jindi.infra.dataapi.oneservice.locator.OneServiceDiscoveryLocator;
import com.jindi.infra.dataapi.oneservice.locator.OneServiceUrlLocator;
import com.jindi.infra.dataapi.oneservice.properties.OneServiceConfigProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneServiceConfigPropertiesRefresher implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static final String ONE_SERVICE_PROPERTIES_BEAN_NAME = "oneservice.config-com.jindi.infra.dataapi.oneservice.properties.OneServiceConfigProperties";
    private static final String ONE_SERVICE_PROPERTIES_PREFIX = "oneservice.config";

    @Resource
    private OneServiceUrlLocator oneServiceUrlLocator;
    @Resource
    private OneServiceDiscoveryLocator oneServiceDiscoveryLocator;
    @Resource
    private OneServiceConfigProperties oneServiceConfigProperties;

    @ApolloConfigChangeListener({OneServiceConsts.ONE_SERVICE_NAMESPACE, ConfigConsts.NAMESPACE_APPLICATION})
    public void onChange(ConfigChangeEvent event) {
        Set<String> keys = event.changedKeys();
        for (String key : keys) {
            if (key.startsWith(ONE_SERVICE_PROPERTIES_PREFIX)) {
                log.info("k:v -> {}:{}", key, event.getChange(key).getNewValue());
                this.applicationContext.getAutowireCapableBeanFactory().configureBean(oneServiceConfigProperties, ONE_SERVICE_PROPERTIES_BEAN_NAME);
                oneServiceUrlLocator.refresh(oneServiceConfigProperties);
                oneServiceDiscoveryLocator.refresh(oneServiceConfigProperties);
                break;
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
