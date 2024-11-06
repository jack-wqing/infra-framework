package com.jindi.infra.datasource.dsfactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.jindi.infra.datasource.utils.DataSourcePropertiesUtils;
import com.jindi.infra.tools.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


@Slf4j
public class DruidDSFactory extends BaseDataSourceFactory {

    private static Set<String> focusField = new HashSet<>();

    @Override
    public String poolType() {
        return "druid";
    }

    @Override
    public DataSource createDataSource(String beanName, Properties fixedProperties, Properties properties) {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(DataSourcePropertiesUtils.getJdbcUrl(properties));
        ds.setDriverClassName(DataSourcePropertiesUtils.getJdbcDriverClass(properties));
        ds.setUsername(DataSourcePropertiesUtils.getJdbcUserName(properties));
        ds.setPassword(DataSourcePropertiesUtils.getJdbcPassword(properties));
        ds.setConnectProperties(PropertiesUtils.toCamel(properties));
        ds.setName(beanName);
        try {
            ds.init();
        } catch (SQLException e) {
            log.error("DruidDataSource初始化失败, {}", beanName, e);
        }
        return ds;
    }
}