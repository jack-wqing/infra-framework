package com.jindi.infra.datasource;

import com.jindi.infra.datasource.config.ExecuteLimitConfig;
import com.jindi.infra.datasource.config.ExecuteLimitProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.datasource.config.ExecuteTimeoutConfig;
import com.jindi.infra.datasource.config.ExecuteTimeoutProperties;
import com.jindi.infra.datasource.processor.druid.DruidConfigBeanPostProcessor;
import com.jindi.infra.datasource.processor.hikari.HikariConfigBeanPostProcessor;

@Configuration
@ConditionalOnProperty(name = "datasource.enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = SqlSessionFactory.class)
public class InfraDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExecuteTimeoutProperties executeTimeoutProperties() {
        return new ExecuteTimeoutProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecuteTimeoutConfig executeTimeoutConfig() {
        return new ExecuteTimeoutConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecuteLimitProperties executeLimitProperties() {
        return new ExecuteLimitProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecuteLimitConfig executeLimitConfig() {
        return new ExecuteLimitConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.zaxxer.hikari.HikariDataSource")
    public HikariConfigBeanPostProcessor hikariConfigBeanPostProcessor() {
        return new HikariConfigBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")
    public DruidConfigBeanPostProcessor druidConfigBeanPostProcessor() {
        return new DruidConfigBeanPostProcessor();
    }
}
