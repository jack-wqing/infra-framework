package com.jindi.infra.trace.mysql;

import com.jindi.infra.trace.model.TraceContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SqlTracingInterceptorDefaultConfig implements InitializingBean {

    @Autowired(required = false)
    private TraceContext traceContext;
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Override
    public void afterPropertiesSet() {
        if (traceContext == null || CollectionUtils.isEmpty(sqlSessionFactoryList)) {
            return;
        }
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            String key = getKey(sqlSessionFactory);
            sqlSessionFactory.getConfiguration().addInterceptor(new SqlTracingInterceptor(key, getJdbc(sqlSessionFactory), traceContext));
        }
    }

    private String getKey(SqlSessionFactory sqlSessionFactory) {
        return sqlSessionFactory.getConfiguration().getDatabaseId();
    }

    private String getJdbc(SqlSessionFactory sqlSessionFactory) {
        return sqlSessionFactory.getConfiguration().getDatabaseId();
    }
}
