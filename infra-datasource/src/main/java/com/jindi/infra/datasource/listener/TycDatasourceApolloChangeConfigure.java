package com.jindi.infra.datasource.listener;


import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloProcessor;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.jindi.infra.datasource.annotation.TycDataSourceChangeListener;
import com.jindi.infra.datasource.properties.TycDataSourceHolder;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import com.jindi.infra.datasource.utils.TycDataSourcePropertiesUtils;
import com.jindi.infra.tools.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TycDatasourceApolloChangeConfigure extends ApolloProcessor implements BeanFactoryAware, EnvironmentAware {

    private static final String NAMESPACE_DELIMITER = ",";
    private static final String PREFIX = "spring.datasource.";

    private static final Splitter NAMESPACE_SPLITTER = Splitter.on(NAMESPACE_DELIMITER)
            .omitEmptyStrings().trimResults();

    private Environment environment;

    private BeanFactory beanFactory;

    public static Set<String> prefixSet = null;

    @Override
    protected void processField(Object bean, String beanName, Field field) {
    }

    @Override
    protected void processMethod(final Object bean, String beanName, final Method method) {
        if (prefixSet == null) {
            fillPrefix();
        }
        this.processApolloConfigChangeListener(bean, method);
    }

    private void fillPrefix() {
        prefixSet = new HashSet<>();
        TycDataSourceProperties infraDataSourceProperties = TycDataSourcePropertiesUtils.getProperties(environment, beanFactory);
        for (TycDataSourceHolder holder : infraDataSourceProperties.values()) {
            if (StringUtils.isEmpty(holder.getPrefix())) {
                prefixSet.add(PREFIX + holder.getBeanName());
            } else {
                prefixSet.add(holder.getPrefix());
            }
        }
    }

    private void processApolloConfigChangeListener(final Object bean, final Method method) {
        TycDataSourceChangeListener annotation = AnnotationUtils
                .findAnnotation(method, TycDataSourceChangeListener.class);
        if (annotation == null) {
            return;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Preconditions.checkArgument(parameterTypes.length == 1,
                "Invalid number of parameters: %s for method: %s, should be 1", parameterTypes.length,
                method);
        Preconditions.checkArgument(ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0]),
                "Invalid parameter type: %s for method: %s, should be ConfigChangeEvent", parameterTypes[0],
                method);

        ReflectionUtils.makeAccessible(method);
        String[] annotatedInterestedKeyPrefixes = prefixSet.toArray(new String[0]);
        ConfigChangeListener configChangeListener = changeEvent -> ReflectionUtils.invokeMethod(method, bean, changeEvent);

        Set<String> interestedKeyPrefixes =
                annotatedInterestedKeyPrefixes.length > 0 ? Sets.newHashSet(annotatedInterestedKeyPrefixes)
                        : null;

        Set<String> resolvedNamespaces = getAllNamespace();

        for (String namespace : resolvedNamespaces) {
            Config config = ConfigService.getConfig(namespace);

            config.addChangeListener(configChangeListener, null, interestedKeyPrefixes);
        }
    }

    private Set<String> getAllNamespace() {
        Properties prefix = PropertiesUtils.getPropertiesByPrefix(environment, "apollo.bootstrap.namespaces");
        if (MapUtils.isEmpty(prefix)) {
            return new HashSet<>();
        }
        return prefix.values().stream().map(String::valueOf).map(namespace
                -> Arrays.stream(namespace.split(",")).collect(Collectors.toList()))
                .flatMap(Collection::stream).filter(StringUtils::isNoneBlank)
                .map(String::trim).collect(Collectors.toSet());
    }

    /**
     * Evaluate and resolve namespaces from env/properties.
     * Split delimited namespaces
     * @param namespaces
     * @return resolved namespaces
     */
    private Set<String> processResolveNamespaceValue(Set<String> namespaces) {

        Set<String> resolvedNamespaces = new HashSet<>();

        for (String namespace : namespaces) {
            final String resolvedNamespace = this.environment.resolveRequiredPlaceholders(namespace);

            if (resolvedNamespace.contains(NAMESPACE_DELIMITER)) {
                resolvedNamespaces.addAll(NAMESPACE_SPLITTER.splitToList(resolvedNamespace));
            } else {
                resolvedNamespaces.add(resolvedNamespace);
            }
        }

        return resolvedNamespaces;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}
