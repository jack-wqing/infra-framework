package com.jindi.infra.datasource.processor;


import com.jindi.infra.datasource.properties.TycDataSourceHolder;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import com.jindi.infra.tools.util.BeanUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TycDataSourceBeanFactoryPostProcessor implements BeanFactoryPostProcessor, BeanFactoryAware, Ordered {

    private static BeanFactory beanFactory;
    private static TycDataSourceProperties infraDataSourceProperties;
    private static Set<String> infraDataSource;

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1000;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            infraDataSource = new HashSet<>();
            infraDataSourceProperties = beanFactory.getBean(TycDataSourceProperties.class);
            if (infraDataSourceProperties == null) {
                return;
            }
            for (Map.Entry<String, TycDataSourceHolder> entry : infraDataSourceProperties.entrySet()) {
                try {
                    registerDataSource(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    log.error("=============初始化{}出错===========", entry.getKey(), e);
                    if (infraDataSourceProperties.getNeedGenEmptyDataSource()) {
                        registerEmptyDataSource(entry.getKey());
                    } else {
                        throw e;
                    }
                }
            }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        TycDataSourceBeanFactoryPostProcessor.beanFactory = beanFactory;
    }

    private void registerDataSource(String name, TycDataSourceHolder infraDataSourceHolder) {
        DataSource dataSource = infraDataSourceHolder.getDataSource();
        BeanUtils.registerIfAbsent(beanFactory, name, dataSource);
        infraDataSource.add(name);
    }

    private void registerEmptyDataSource(String name) {
        BeanUtils.registerIfAbsent(beanFactory, name, new HikariDataSource());
        infraDataSource.add(name);
        log.info("=============兜底空初始化{}完成===========", name);
    }

    public static Boolean isInfraDataSource(String dataSourceName) {
        return infraDataSource.contains(dataSourceName);
    }
}
