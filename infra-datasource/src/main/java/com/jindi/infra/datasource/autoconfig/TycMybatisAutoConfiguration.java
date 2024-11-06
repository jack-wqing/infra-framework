package com.jindi.infra.datasource.autoconfig;


import com.alibaba.druid.pool.DruidDataSource;
import com.jindi.infra.datasource.dsfactory.DruidDSFactory;
import com.jindi.infra.datasource.dsfactory.HikariDSFactory;
import com.jindi.infra.datasource.listener.TycDataSourcePropertiesChangeListener;
import com.jindi.infra.datasource.listener.TycDatasourceApolloChangeConfigure;
import com.jindi.infra.datasource.metrics.DruidDataSourceMetricsBinder;
import com.jindi.infra.datasource.metrics.HikariDataSourceMetricsBinder;
import com.jindi.infra.datasource.processor.TycDataSourceBeanFactoryPostProcessor;
import com.jindi.infra.datasource.processor.TycDataSourcePropertiesBeanFactoryPostprocessor;
import com.jindi.infra.datasource.processor.TycMapperScanBeanDefinitionRegistryPostProcessor;
import com.jindi.infra.datasource.processor.TycMybatisBeanFactoryPostprocessor;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass({ SqlSessionFactory.class, MybatisProperties.class, DataSource.class})
@Import({TycMybatisAutoConfiguration.HikariConfiguration.class, TycMybatisAutoConfiguration.DruidConfiguration.class})
public class TycMybatisAutoConfiguration {

    @Bean
    @ConditionalOnClass({DataSource.class})
    @ConditionalOnMissingBean
    public TycDataSourceBeanFactoryPostProcessor infraDataSourceBeanFactoryPostProcessor() {
        return new TycDataSourceBeanFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnClass({TycDataSourceProperties.class})
    @ConditionalOnMissingBean
    public TycDataSourcePropertiesBeanFactoryPostprocessor infraDataSourcePropertiesBeanFactoryPostprocessor() {
        return new TycDataSourcePropertiesBeanFactoryPostprocessor();
    }

    @Bean
    @ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
    @ConditionalOnMissingBean
    public TycMybatisBeanFactoryPostprocessor infraMybatisBeanFactoryPostprocessor() {
        return new TycMybatisBeanFactoryPostprocessor();
    }

    @Bean
    @ConditionalOnClass({MapperScannerConfigurer.class})
    @ConditionalOnMissingBean
    public TycMapperScanBeanDefinitionRegistryPostProcessor infraMapperScanBeanFactoryPostprocessor() {
        return new TycMapperScanBeanDefinitionRegistryPostProcessor();
    }

    @Configuration
    @ConditionalOnClass(DruidDataSource.class)
    public static class DruidConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DruidDataSourceMetricsBinder druidDataSourceMetricsBinder() {
            return new DruidDataSourceMetricsBinder();
        }

        @Bean
        @ConditionalOnMissingBean
        public DruidDSFactory druidDSFactory() {
            return new DruidDSFactory();
        }
    }

    @Configuration
    @ConditionalOnClass(HikariDataSource.class)
    public static class HikariConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public HikariDataSourceMetricsBinder hikariDataSourceMetricsBinder() {
            return new HikariDataSourceMetricsBinder();
        }

        @Bean
        @ConditionalOnMissingBean
        public HikariDSFactory hikariDSFactory() {
            return new HikariDSFactory();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public TycDatasourceApolloChangeConfigure infraDatasourceApolloChangeConfigure() {
        return new TycDatasourceApolloChangeConfigure();
    }

    @Bean
    @ConditionalOnMissingBean
    public TycDataSourcePropertiesChangeListener infraDataSourcePropertiesChangeListener(MeterRegistry registry) {
        return new TycDataSourcePropertiesChangeListener(registry);
    }

}
