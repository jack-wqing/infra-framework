package com.jindi.infra.datasource.processor;


import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import com.jindi.infra.tools.util.BeanUtils;
import com.jindi.infra.datasource.utils.TycDataSourcePropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

@Slf4j
public class TycDataSourcePropertiesBeanFactoryPostprocessor implements BeanFactoryAware, EnvironmentAware, BeanDefinitionRegistryPostProcessor, Ordered {

    private Environment environment;

    private BeanFactory beanFactory;

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 100;
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
        TycDataSourceProperties infraDataSourceProperties = TycDataSourcePropertiesUtils.getProperties(environment, beanFactory);

        BeanUtils.registerIfAbsent(beanFactory, "infraDataSourceProperties", infraDataSourceProperties);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }




}
