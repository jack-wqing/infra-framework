package com.jindi.infra.datasource.properties;


import com.jindi.infra.datasource.dsfactory.BaseDataSourceFactory;
import com.jindi.infra.datasource.exception.TycDataSourceException;
import com.jindi.infra.datasource.utils.DynamicObjectProxy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

@Data
@Slf4j
public class TycDataSourceHolder {

    /**
     * 数据源名称
     */
    private String beanName;

    /**
     * Mapper接口类的位置
     */
    private List<String> basePackages = new ArrayList<>();

    /**
     * Mapper接口类的位置
     */
    private List<Class<?>> basePackageClasses = new ArrayList<>();

    /**
     * mapper.xml的位置
     */
    private List<String> mapperLocation = new ArrayList<>();

    /**
     * 针对当前数据源的mybatis配置
     */
    private MybatisProperties mybatisProperties;

    /**
     * 如果需要当前数据源注册任务,可以定义下事务管理器的名称
     */
    private String transactionManagerName = "";

    /**
     * mybatis的配置,需要通过bean注入方式
     */
    private MybatisBeanConfiguration mybatisBeanConfiguration = new MybatisBeanConfiguration();

    /**
     * 数据源获取的前缀,默认是  spring.datasource.数据源名称
     */
    private String prefix = "";

    private Properties properties = new Properties();

    public DataSource getDataSource() {
        return DynamicObjectProxy.wrap(BaseDataSourceFactory.create(beanName, properties));
    }

    @Data
    public static class MybatisBeanConfiguration {
        /**
         * interceptor实现类,可以是类名,也可以是一个BeanName
         */
        private List<String> interceptors = new ArrayList<>();

        private List<String> typeHandlers = new ArrayList<>();

        private String databaseIdProvider;

        private List<String> languageDrivers = new ArrayList<>();

        private List<String> configurationCustomizers = new ArrayList<>();
    }


}
