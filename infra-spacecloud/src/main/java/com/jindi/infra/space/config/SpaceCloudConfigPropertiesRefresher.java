package com.jindi.infra.space.config;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.jindi.infra.space.SpaceCloudUrlLocator;
import com.jindi.infra.space.properties.RomaConfigProperties;
import com.jindi.infra.space.properties.SpaceCloudConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.util.Set;

@Slf4j
public class SpaceCloudConfigPropertiesRefresher implements ApplicationContextAware {

    private static final String SPACE_CLOUD_NAMESPACE = "TYC-COMMON.spacecloud";
    private static final String ROMA_PROPERTIES_BEAN_NAME = "roma.config-com.jindi.infra.space.properties.RomaConfigProperties";
    private static final String ROMA_PROPERTIES_PREFIX = "roma.config";
    private ApplicationContext applicationContext;
    private static final String SPACE_CLOUD_PROPERTIES_BEAN_NAME = "spacecloud.config-com.jindi.infra.space.properties.SpaceCloudConfigProperties";
    private static final String SPACE_CLOUD_PROPERTIES_PREFIX = "spacecloud.config";

    @Resource
    private SpaceCloudUrlLocator spaceCloudUrlLocator;
    @Resource
    private SpaceCloudConfigProperties spaceCloudConfigProperties;
    @Resource
    private RomaConfigProperties romaConfigProperties;

    @ApolloConfigChangeListener({SPACE_CLOUD_NAMESPACE, ConfigConsts.NAMESPACE_APPLICATION})
    public void onChange(ConfigChangeEvent event) {
        Set<String> keys = event.changedKeys();
        for (String key : keys) {
            if (key.startsWith(SPACE_CLOUD_PROPERTIES_PREFIX)) {
                this.applicationContext.getAutowireCapableBeanFactory().configureBean(spaceCloudConfigProperties, SPACE_CLOUD_PROPERTIES_BEAN_NAME);
                spaceCloudUrlLocator.refresh(spaceCloudConfigProperties);
                break;
            }
        }
        for (String key : keys) {
            if (key.startsWith(ROMA_PROPERTIES_PREFIX)) {
                this.applicationContext.getAutowireCapableBeanFactory().configureBean(romaConfigProperties, ROMA_PROPERTIES_BEAN_NAME);
                break;
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
