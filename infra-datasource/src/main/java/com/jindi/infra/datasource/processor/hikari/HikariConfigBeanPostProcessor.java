package com.jindi.infra.datasource.processor.hikari;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import com.zaxxer.hikari.HikariDataSource;

public class HikariConfigBeanPostProcessor implements BeanPostProcessor, Ordered {

    public static final String SCOPED_TARGET_PREFIX = "scopedTarget.";
    public static final String COMMON_PREFIX = "common";

    @Value("${hikari.datasource.connectionTimeout:1000}")
    private Long connectionTimeout;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) bean;
            if (hikariDataSource.getHikariConfigMXBean() != null) {
                long connectionTimeout = hikariDataSource.getHikariConfigMXBean().getConnectionTimeout();
                if (connectionTimeout > 20000) {
                    hikariDataSource.getHikariConfigMXBean().setConnectionTimeout(this.connectionTimeout);
                }
            }
            if (StringUtils.isBlank(hikariDataSource.getPoolName()) && !beanName.startsWith(COMMON_PREFIX)) {
                String subBeanName = subBeanName(beanName);
                hikariDataSource.setPoolName(subBeanName);
            }
            return hikariDataSource;
        }
        return bean;
    }

    private String subBeanName(String beanName) {
        if(beanName.startsWith(SCOPED_TARGET_PREFIX)) {
            beanName = beanName.substring(SCOPED_TARGET_PREFIX.length());
        }
        return beanName;
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
