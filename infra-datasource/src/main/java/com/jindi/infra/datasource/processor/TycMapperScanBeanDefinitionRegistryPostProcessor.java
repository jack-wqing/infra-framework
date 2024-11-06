package com.jindi.infra.datasource.processor;


import com.jindi.infra.datasource.properties.TycDataSourceHolder;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TycMapperScanBeanDefinitionRegistryPostProcessor implements BeanFactoryAware, BeanDefinitionRegistryPostProcessor, Ordered {

    private BeanFactory beanFactory;

    @Autowired
    private TycDataSourceProperties infraDataSourceProperties;

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1000;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)  throws BeansException {
        if (infraDataSourceProperties == null)  {
            fillInfraDataSourceProperties(beanFactory);
        }
        if (MapUtils.isEmpty(infraDataSourceProperties)) {
            return;
        }
        infraDataSourceProperties.values().forEach(holder->registerMapperScanConfigure(registry, holder));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private void registerMapperScanConfigure(BeanDefinitionRegistry registry, TycDataSourceHolder holder) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", true);
        builder.addPropertyValue("sqlSessionFactoryBeanName", holder.getBeanName() + "SqlSessionFactory");
        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(holder.getBasePackages().stream().filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(holder.getBasePackageClasses().stream().map(ClassUtils::getPackageName).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(basePackages)) {
            log.warn("{}的basePackages配置为空", holder.getBeanName());
            return;
        }
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));
        registry.registerBeanDefinition(holder.getBeanName() + "MapperConfigure", builder.getBeanDefinition());
    }

    private void fillInfraDataSourceProperties(BeanFactory beanFactory) {
        try {
            infraDataSourceProperties = beanFactory.getBean(TycDataSourceProperties.class);
        } catch (Exception e) {
            // 无额外配置
        }
    }
}
