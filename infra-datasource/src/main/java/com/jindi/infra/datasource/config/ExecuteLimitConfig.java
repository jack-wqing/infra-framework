package com.jindi.infra.datasource.config;

import java.util.List;

import com.jindi.infra.datasource.interceptor.SqlQueryLimitCountInterceptor;
import com.jindi.infra.datasource.interceptor.SqlQuerySizeCatInterceptor;
import io.prometheus.client.CollectorRegistry;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;


public class ExecuteLimitConfig implements InitializingBean {

    @Autowired
    private ExecuteLimitProperties properties;
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactories;
    @Autowired(required = false)
    private CollectorRegistry collectorRegistry;
    @Value("${spring.application.name}")
    private String application;
    @Value("${spring.profiles.active:}")
    private String env;

    @Override
    public void afterPropertiesSet() {
        if(!CollectionUtils.isEmpty(sqlSessionFactories)) {
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                sqlSessionFactory.getConfiguration().addInterceptor(new SqlQueryLimitCountInterceptor(properties));
                sqlSessionFactory.getConfiguration().addInterceptor(new SqlQuerySizeCatInterceptor(application, env, properties, collectorRegistry));
            }
        }
    }
}
