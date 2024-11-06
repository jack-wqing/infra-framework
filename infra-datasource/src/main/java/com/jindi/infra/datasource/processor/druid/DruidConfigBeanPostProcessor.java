package com.jindi.infra.datasource.processor.druid;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.core.Ordered;

public class DruidConfigBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Value("${druid.datasource.maxWait:1000}")
    private Long maxWait;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) bean;
            long maxWait = druidDataSource.getMaxWait();
            if (maxWait > 10000 || maxWait < 0) {
                druidDataSource.setMaxWait(this.maxWait);
            }
            return druidDataSource;
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
