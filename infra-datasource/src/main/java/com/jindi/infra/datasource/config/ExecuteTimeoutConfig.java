package com.jindi.infra.datasource.config;

import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.datasource.interceptor.SqlQueryExecuteTimeInterceptor;

public class ExecuteTimeoutConfig implements InitializingBean {

    @Autowired
    private ExecuteTimeoutProperties properties;
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactories;

    @Override
    public void afterPropertiesSet() {
        if(!CollectionUtils.isEmpty(sqlSessionFactories)) {
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                sqlSessionFactory.getConfiguration().addInterceptor(new SqlQueryExecuteTimeInterceptor(properties));
            }
        }
    }
}
