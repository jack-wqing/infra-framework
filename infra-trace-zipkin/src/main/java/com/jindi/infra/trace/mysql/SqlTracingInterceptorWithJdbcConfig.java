package com.jindi.infra.trace.mysql;

import com.jindi.infra.datasource.metrics.DataSourceMetricsBinder;
import com.jindi.infra.trace.model.TraceContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.List;

public class SqlTracingInterceptorWithJdbcConfig implements InitializingBean {

    @Autowired(required = false)
    private TraceContext traceContext;
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactoryList;
    @Autowired(required = false)
    private List<DataSourceMetricsBinder> dataSourceMetricsBinders;

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
        DataSourceMetricsBinder binder = selectBinder();
        if (binder == null || sqlSessionFactory.getConfiguration().getEnvironment() == null
                || sqlSessionFactory.getConfiguration().getEnvironment().getDataSource() == null) {
            return sqlSessionFactory.getConfiguration().getDatabaseId();
        }
        return binder.getPoolName(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource());
    }

    private String getJdbc(SqlSessionFactory sqlSessionFactory) {
        DataSourceMetricsBinder binder = selectBinder();
        if (binder == null || sqlSessionFactory.getConfiguration().getEnvironment() == null
                || sqlSessionFactory.getConfiguration().getEnvironment().getDataSource() == null) {
            return "";
        }
        DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        return binder.getInstance(dataSource) + binder.getDataBase(dataSource);
    }

    private DataSourceMetricsBinder selectBinder() {
        if (CollectionUtils.isEmpty(dataSourceMetricsBinders)) {
            return null;
        }
        return dataSourceMetricsBinders.get(0);
    }
}
