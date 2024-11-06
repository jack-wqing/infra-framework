package com.jindi.infra.cache.redis.processor;


import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.cache.redis.properties.TycRedisProperties;
import com.jindi.infra.tools.util.BeanUtils;
import com.jindi.infra.tools.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class TycRedisPropertiesPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered, BeanFactoryAware {

    private Environment environment;

    private static Set<String> IGNORE_PREFIX_NAME;

    private BeanFactory beanFactory;

    static {
        IGNORE_PREFIX_NAME = new HashSet<>();
        IGNORE_PREFIX_NAME.add("common");
        IGNORE_PREFIX_NAME.add("pool");
        IGNORE_PREFIX_NAME.add("jedis");
        IGNORE_PREFIX_NAME.add("lettuce");
        IGNORE_PREFIX_NAME.add("sentinel");
        IGNORE_PREFIX_NAME.add("cluster");
        IGNORE_PREFIX_NAME.addAll(Arrays.stream(RedisProperties.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList()));
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Properties properties = PropertiesUtils.getPropertiesByPrefix(environment, "spring.redis");

        Properties commonProperties = PropertiesUtils.getPropertiesByPrefix(properties, "common");

        TycRedisProperties tycInfraProperties = genTycRedisProperties(properties, commonProperties);

        tycInfraProperties.setCommonProperties(commonProperties);

        BeanUtils.registerIfAbsent(beanFactory, "tycRedisProperties", tycInfraProperties);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private TycRedisProperties genTycRedisProperties(Properties properties, Properties commonProperties) {
        TycRedisProperties tycRedisProperties = new TycRedisProperties();
        properties.forEach((key, value) -> {
            String clientName = getClientName(key);
            if (StringUtils.isBlank(clientName) || IGNORE_PREFIX_NAME.contains(clientName)) {
                return;
            }
            if (!tycRedisProperties.containsKey(clientName)) {
                tycRedisProperties.put(clientName, new TycRedisHolder(clientName, commonProperties));
            }
            tycRedisProperties.get(clientName).put(getValueKey(key), value);
        });
        return tycRedisProperties;
    }

    private static String getClientName(Object key) {
        String keyStr = String.valueOf(key);
        if (!keyStr.contains(".")) {
            return null;
        }
        return keyStr.substring(0, keyStr.indexOf("."));
    }

    private static String getValueKey(Object key) {
        String keyStr = String.valueOf(key);
        if (!keyStr.contains(".")) {
            return keyStr;
        }
        return keyStr.substring(keyStr.indexOf(".") + 1);
    }

}
