package com.jindi.infra.tools.thread;


import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.annotation.ApolloProcessor;
import com.google.common.collect.Sets;
import com.jindi.infra.tools.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicThreadPoolConfigure extends ApolloProcessor implements EnvironmentAware, ApplicationListener<ApplicationStartedEvent> {

    private static final String PREFIX = "infra.thread.pool.";

    private static Environment environment;

    private static DynamicThreadPoolListener dynamicThreadPoolListener;

    @Override
    protected void processField(Object bean, String beanName, Field field) {
    }

    @Override
    protected void processMethod(final Object bean, String beanName, final Method method) {
        if (bean instanceof DynamicThreadPoolListener && "refresh".equals(method.getName())) {
            dynamicThreadPoolListener = (DynamicThreadPoolListener) bean;
            this.processApolloConfigChangeListener((DynamicThreadPoolListener) bean, method);
        }
    }

    private void processApolloConfigChangeListener(final DynamicThreadPoolListener listener, final Method method) {
        ReflectionUtils.makeAccessible(method);
        ConfigChangeListener configChangeListener = changeEvent -> ReflectionUtils.invokeMethod(method, listener, changeEvent);
        Set<String> interestedKeyPrefixes = Sets.newHashSet(PREFIX);
        Set<String> resolvedNamespaces = getAllNamespace();

        for (String namespace : resolvedNamespaces) {
            Config config = ConfigService.getConfig(namespace);
            config.addChangeListener(configChangeListener, null, interestedKeyPrefixes);
        }
    }

    private Set<String> getAllNamespace() {
        Properties prefix = PropertiesUtils.getPropertiesByPrefix(environment, "apollo.bootstrap.namespaces");
        if (CollectionUtils.isEmpty(prefix)) {
            return new HashSet<>();
        }
        return prefix.values().stream().map(String::valueOf).map(namespace
                -> Arrays.stream(namespace.split(",")).collect(Collectors.toList()))
                .flatMap(Collection::stream).filter(StringUtils::isNoneBlank)
                .map(String::trim).collect(Collectors.toSet());
    }

    @Override
    public void setEnvironment(Environment env) {
        environment = env;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (dynamicThreadPoolListener == null) {
            return;
        }
        Properties prefix = PropertiesUtils.getPropertiesByPrefix(environment, PREFIX);
        if (CollectionUtils.isEmpty(prefix)) {
            return;
        }
        Map<String, String> properties = getProperty(prefix);
        dynamicThreadPoolListener.refreshInternal(properties);
    }

    private Map<String, String> getProperty(Properties properties) {
        Map<String, String> result = new HashMap<>();
        for (Object key : properties.keySet()) {
            result.put(key.toString(), properties.get(key).toString());
        }
        return result;
    }
}
