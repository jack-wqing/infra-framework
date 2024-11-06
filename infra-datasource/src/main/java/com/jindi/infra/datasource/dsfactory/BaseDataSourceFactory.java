package com.jindi.infra.datasource.dsfactory;

import com.jindi.infra.datasource.exception.TycDataSourceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;


@Slf4j
public abstract class BaseDataSourceFactory implements BeanFactoryAware {

    private static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory localBeanFactory) throws BeansException {
        beanFactory = localBeanFactory;
    }

    public abstract String poolType();

    protected Properties fixProperties(Properties properties) {
        return properties;
    }

    protected abstract DataSource createDataSource(String beanName, Properties fixedProperties, Properties properties);

    public DataSource getDataSource(String beanName, Properties properties) {
        Properties fixedProperties = fixProperties(properties);
        return createDataSource(beanName, fixedProperties, properties);
    }

    private static List<BaseDataSourceFactory> dataSourceFactories = new ArrayList<>();

    private static Set<String> dsTypeList = new HashSet<>(Arrays.asList("druid", "hikari", "c3p0", "dbcp", "tomcat", "dbcp2"));

    public static DataSource create(String beanName, Properties properties) {
        log.info("=============初始化{}开始===========, properties:{}", beanName, properties);
        String poolType = getPoolType(properties);
        Optional<BaseDataSourceFactory> dataSourceFactory = getDSFactories().stream().filter(factory->factory.poolType().equals(poolType)).findFirst();
        if (!dataSourceFactory.isPresent()) {
            throw new TycDataSourceException("暂时不支持该数据源类型");
        }
        DataSource dataSource = dataSourceFactory.get().getDataSource(beanName, properties);
        log.info("=============初始化{}完成===========", beanName);
        return dataSource;
    }

    private static String getPoolType(Properties properties) {
        String poolType = properties.getProperty("type");
        if (StringUtils.isNotBlank(poolType)) {
            for (String dsType : dsTypeList) {
                if (poolType.toLowerCase().contains(dsType)) {
                    return dsType;
                }
            }
        }
        for (String dsType : dsTypeList) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (String.valueOf(entry.getKey()).startsWith(dsType)) {
                    return dsType;
                }
            }
        }
        return "hikari";
    }

    private static List<BaseDataSourceFactory> getDSFactories() {
        if (!CollectionUtils.isEmpty(dataSourceFactories)) {
            return dataSourceFactories;
        }
        try {
           dataSourceFactories.add(new DruidDSFactory());
           dataSourceFactories.add(new HikariDSFactory());
           return dataSourceFactories;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

}