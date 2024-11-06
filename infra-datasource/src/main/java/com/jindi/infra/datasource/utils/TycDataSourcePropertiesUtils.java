package com.jindi.infra.datasource.utils;


import com.google.common.collect.Lists;
import com.jindi.infra.tools.util.BeanUtils;
import com.jindi.infra.tools.util.PropertiesUtils;
import com.jindi.infra.datasource.annotation.TycDataSource;
import com.jindi.infra.datasource.annotation.TycDataSourceConfig;
import com.jindi.infra.datasource.annotation.TycMybatisProperties;
import com.jindi.infra.datasource.annotation.TycMybatisProperty;
import com.jindi.infra.datasource.exception.TycDataSourceException;
import com.jindi.infra.datasource.properties.TycDataSourceHolder;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TycDataSourcePropertiesUtils {


    private static Set<String> IGNORE_PREFIX_NAME;
    public static final String SPRING_DATA_SOURCE_PREFIX = "spring.datasource.";
    private static Map<String, String> localProperties = new HashMap<>();
    private static final String UTF8_REGEX = "[ ]*((UTF8MB4)|(utf8mb4)|(UTF8)|(utf8))[ ]*";

    static {
        IGNORE_PREFIX_NAME = new HashSet<>();
        IGNORE_PREFIX_NAME.add("mybatis");
        IGNORE_PREFIX_NAME.add("common");
        IGNORE_PREFIX_NAME.add("hikari");
        IGNORE_PREFIX_NAME.add("druid");
        IGNORE_PREFIX_NAME.add("dataSource");
        IGNORE_PREFIX_NAME.addAll(Arrays.stream(DataSourceProperties.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList()));
    }

    public static TycDataSourceProperties getProperties(Environment environment, BeanFactory beanFactory) {
        try {
            TycDataSourceProperties infraDataSourceProperties = initPropertiesWithAnnotation(beanFactory);

            fillByEnvProperties(infraDataSourceProperties, environment);

            removeInvalidHolder(infraDataSourceProperties);

            return infraDataSourceProperties;
        } catch (Exception e) {
            log.error("初始化配置出现异常", e);
            throw new TycDataSourceException("初始化配置出现异常, 请检查多数据源配置");
        }
    }

    /**
     * 根据注解完成InfraDataSourceProperties初始化
     * @param beanFactory
     * @return
     */
    private static TycDataSourceProperties initPropertiesWithAnnotation(BeanFactory beanFactory) {
        TycDataSourceProperties infraMySQLProperties = new TycDataSourceProperties();
        Map<String, Object> beansWithAnnotation = BeanUtils.getBeansWithAnnotation(beanFactory, TycDataSourceConfig.class);
        for (Object annotatedBean : beansWithAnnotation.values()) {
            List<Field> annotatedFields = getInfraDataSourceFields(annotatedBean);
            List<TycMybatisProperty> commonMybatisProperties = getInfraMybatisProperty(annotatedBean);
            for (Field annotatedField : annotatedFields) {
                TycDataSourceHolder holder = initPropertiesWithAnnotation(annotatedField, commonMybatisProperties);
                holder.setBeanName(annotatedField.getName());
                infraMySQLProperties.put(annotatedField.getName(), holder);
            }
        }
        return infraMySQLProperties;
    }

    private static List<TycMybatisProperty> getInfraMybatisProperty(Object annotatedBean) {
        List<TycMybatisProperty> properties = new ArrayList<>();
        if (annotatedBean.getClass().isAnnotationPresent(TycMybatisProperties.class)) {
            TycMybatisProperties annotation = annotatedBean.getClass().getAnnotation(TycMybatisProperties.class);
            properties.addAll(Arrays.asList(annotation.value()));
        }
        if (annotatedBean.getClass().isAnnotationPresent(TycMybatisProperty.class)) {
            TycMybatisProperty infraMybatisProperty = annotatedBean.getClass().getAnnotation(TycMybatisProperty.class);
            properties.add(infraMybatisProperty);
        }
        return properties;
    }

    private static TycDataSourceHolder initPropertiesWithAnnotation(Field annotatedField, List<TycMybatisProperty> commonMybatisProperties) {
        TycDataSource infraDataSource = annotatedField.getAnnotation(TycDataSource.class);
        TycDataSourceHolder holder = new TycDataSourceHolder();
        holder.setBasePackages(Arrays.asList(infraDataSource.basePackages()));
        holder.setBasePackageClasses(Arrays.asList(infraDataSource.basePackageClasses()));
        holder.setMapperLocation(Arrays.asList(infraDataSource.mapperLocation()));
        holder.setTransactionManagerName(infraDataSource.transactionManager());
        holder.setPrefix(infraDataSource.prefix());
        holder.setMybatisBeanConfiguration(new TycDataSourceHolder.MybatisBeanConfiguration());
        fillLocalProperties(annotatedField, commonMybatisProperties, infraDataSource.prefix());
        return holder;
    }

    private static void fillLocalProperties(Field field, List<TycMybatisProperty> commonMybatisProperties, String prefix) {
        if (field.isAnnotationPresent(TycMybatisProperties.class)) {
            TycMybatisProperties annotation = field.getAnnotation(TycMybatisProperties.class);
            for (TycMybatisProperty infraMybatisProperty : annotation.value()) {
                fillLocalProperties(field.getName(), infraMybatisProperty, prefix);
            }
        }
        if (field.isAnnotationPresent(TycMybatisProperty.class)) {
            TycMybatisProperty infraMybatisProperty = field.getAnnotation(TycMybatisProperty.class);
            fillLocalProperties(field.getName(), infraMybatisProperty, prefix);
        }
        if (CollectionUtils.isNotEmpty(commonMybatisProperties)) {
            for (TycMybatisProperty infraMybatisProperty : commonMybatisProperties) {
                fillLocalProperties(field.getName(), infraMybatisProperty, prefix);
            }
        }
    }

    private static void fillLocalProperties(String dataSourceName, TycMybatisProperty property, String prefix) {
        if (StringUtils.isBlank(prefix)) {
            localProperties.put("spring.datasource." + dataSourceName + ".mybatis." + property.key(), property.value());
        } else {
            localProperties.put(prefix + ".mybatis." + property.key(), property.value());
        }
    }

    private static List<Field> getInfraDataSourceFields(Object obj) {
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        if (ArrayUtils.isEmpty(declaredFields)) {
            return new ArrayList<>();
        }
        return Arrays.stream(declaredFields).filter(field->field.isAnnotationPresent(TycDataSource.class)).collect(Collectors.toList());
    }

    /**
     * 获取所有的DataSource配置
     * 1.如果注解@InfraDataSource中配置了前缀,根据前缀获取
     * 2.从environment中根据spring.datasource进行扫描获取
     * @param infraDataSourceProperties
     * @param properties
     * @return
     */
    private static Map<String, Properties> getDataSourceNameList(Environment environment, TycDataSourceProperties infraDataSourceProperties, Properties properties) {
        Map<String, Properties> result = new HashMap<>();
        Map<String, String> dataSourcePrefixMap = infraDataSourceProperties.values().stream()
                .filter(holder->StringUtils.isNotBlank(holder.getPrefix()))
                .collect(Collectors.toMap(TycDataSourceHolder::getBeanName, TycDataSourceHolder::getPrefix));
        fillPrefixProperties(environment, result, dataSourcePrefixMap);
        fillWithoutPrefixProperties(result, properties, dataSourcePrefixMap);
        removeInvalidDataSource(result);
        fixPropertiesPrefix(result);
        fillDefaultProperties(result, PropertiesUtils.getPropertiesByPrefix(properties, "common"));
        return result;
    }

    private static void removeInvalidDataSource(Map<String, Properties> result) {
        List<String> invalidDataSource = result.entrySet().stream().filter(entry->
                StringUtils.isAnyBlank(DataSourcePropertiesUtils.getJdbcPassword(entry.getValue()),
                        DataSourcePropertiesUtils.getJdbcUserName(entry.getValue()),
                        DataSourcePropertiesUtils.getJdbcUrl(entry.getValue())))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(invalidDataSource)) {
            log.warn("无效的数据源配置:{}", invalidDataSource);
            invalidDataSource.forEach(result::remove);
//            throw new InfraDataSourceException("存在出错的数据源配置" + invalidDataSource);
        }
    }

    private static void fixPropertiesPrefix(Map<String, Properties> result) {
        for (Properties properties : result.values()) {
            properties.put("url", adapterJdbcUrl(DataSourcePropertiesUtils.getJdbcUrl(properties)));
            properties.put("jdbcUrl", properties.getProperty("url"));
            properties.putIfAbsent("username", DataSourcePropertiesUtils.getJdbcUserName(properties));
            properties.putIfAbsent("password", DataSourcePropertiesUtils.getJdbcPassword(properties));
            String driverClass = DataSourcePropertiesUtils.getJdbcDriverClass(properties);
            if (StringUtils.isNotBlank(driverClass)) {
                properties.putIfAbsent("driver-class-name", driverClass);
            }
        }
    }

    private static String adapterJdbcUrl(String jdbcUrl) {
        if (StringUtils.isNotBlank(jdbcUrl)) {
            return jdbcUrl.replaceAll("&amp;", "&").replaceAll(UTF8_REGEX, "UTF8");
        }
        return jdbcUrl;
    }

    private static void fillDefaultProperties(Map<String, Properties> result, Properties commonDataSourceProperties) {
        for (Properties properties : result.values()) {
            properties.putIfAbsent("min-idle", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "minIdle", "5"));
            properties.putIfAbsent("minimum-idle", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "minimumIdle", "5"));
            properties.putIfAbsent("max-active", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "maxActive", "200"));
            properties.putIfAbsent("maximum-pool-size", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "maximumPoolSize", "200"));
            properties.putIfAbsent("time-between-eviction-runs-millis", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "timeBetweenEvictionRunsMillis", "300000"));
            properties.putIfAbsent("test-while-idle", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "testWhileIdle", "true"));
            properties.putIfAbsent("validation-query", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "validationQuery", "SELECT 1 FROM DUAL"));
            properties.putIfAbsent("sqlScriptEncoding", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "sqlScriptEncoding", "UTF-8"));
            properties.putIfAbsent("max-idle", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "maxIdle", "5"));
            properties.putIfAbsent("test-on-borrow", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "testOnBorrow", "true"));
            properties.putIfAbsent("sql-script-encoding", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "sqlScriptEncoding", "UTF-8"));
            properties.putIfAbsent("initial-size", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "initialSize", "50"));
            properties.putIfAbsent("min-evictable-ddle-time-millis", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "minEvictableIdleTimeMillis", "1800000"));
            properties.putIfAbsent("test-on-return", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "testOnReturn", "false"));
            properties.putIfAbsent("driver-class-name", PropertiesUtils.getCamelPropertyWithDefault(commonDataSourceProperties, "driverClassName", "com.mysql.jdbc.Driver"));
        }
    }

    private static void fillPrefixProperties(Environment environment, Map<String, Properties> result, Map<String, String> dataSourcePrefixMap) {
        if (MapUtils.isEmpty(dataSourcePrefixMap)) {
            return;
        }
        dataSourcePrefixMap.forEach((dataSource, prefix) -> {
            result.put(dataSource, PropertiesUtils.getPropertiesByPrefix(environment, prefix, TycDataSourcePropertiesUtils.getLocalProperties()));
        });
    }

    private static void fillWithoutPrefixProperties(Map<String, Properties> result, Properties properties, Map<String, String> dataSourcePrefixMap) {
        if (properties == null) {
            return;
        }
        HashSet<String> prefixList = new HashSet<>(dataSourcePrefixMap.values());
        Map<String, Properties> withoutPrefixProperties = new HashMap<>();
        properties.forEach((key, value) -> {
            String dataSource = getDataSource(key);
            if (StringUtils.isBlank(dataSource) || IGNORE_PREFIX_NAME.contains(dataSource) || existDefaultPrefix(prefixList, dataSource)) {
                return;
            }
            if (!withoutPrefixProperties.containsKey(dataSource)) {
                withoutPrefixProperties.put(dataSource, new Properties());
            }
            withoutPrefixProperties.get(dataSource).put(getValueKey(key), value);
        });
        withoutPrefixProperties.forEach((dataSourceName, props) -> {
            if (!result.containsKey(dataSourceName)) {
                result.put(dataSourceName, props);
            }
        });
    }

    private static boolean existDefaultPrefix(HashSet<String> prefixList, String dataSource) {
         return prefixList.contains("spring.datasource." + dataSource);
    }

    private static String getDataSource(Object key) {
        String keyStr = String.valueOf(key);
        if (!keyStr.contains(".")) {
            return null;
        }
        return keyStr.substring(0, keyStr.indexOf("."));
    }

    private static String getValueKey(Object key) {
        String keyStr = String.valueOf(key);
        if (!keyStr.contains(".")) {
            return keyStr;
        }
        return keyStr.substring(keyStr.indexOf(".") + 1);
    }

    /**
     * 根据environment填充InfraDataSourceProperties
     * @param infraDataSourceProperties
     * @param environment
     */
    private static void fillByEnvProperties(TycDataSourceProperties infraDataSourceProperties, Environment environment) throws ClassNotFoundException {
        Properties propertiesByPrefix = PropertiesUtils.getPropertiesByPrefix(environment, SPRING_DATA_SOURCE_PREFIX, localProperties);
        fillCommonMybatisProperties(infraDataSourceProperties, propertiesByPrefix);
        fillCommonProperties(infraDataSourceProperties, propertiesByPrefix);
        fillDataSourceProperties(environment, infraDataSourceProperties, propertiesByPrefix);
    }

    private static void fillCommonProperties(TycDataSourceProperties infraDataSourceProperties, Properties propertiesByPrefix) {
        infraDataSourceProperties.setNeedGenEmptyDataSource("true".equals(propertiesByPrefix.getProperty("common.needGenEmptyDataSource", "false")));
    }

    private static void removeInvalidHolder(TycDataSourceProperties infraDataSourceProperties) {
        List<String> invalidDataSource = infraDataSourceProperties.values().stream().filter(TycDataSourcePropertiesUtils::isInvalidHolder).map(TycDataSourceHolder::getBeanName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(invalidDataSource)) {
            log.warn("无效的数据源配置, basePackage或properties为空:{}", invalidDataSource);
            invalidDataSource.forEach(infraDataSourceProperties::remove);
        }
    }

    private static Boolean isInvalidHolder(TycDataSourceHolder holder) {
        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(holder.getBasePackages().stream().filter(org.springframework.util.StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(holder.getBasePackageClasses().stream().map(ClassUtils::getPackageName).collect(Collectors.toList()));
        return CollectionUtils.isEmpty(basePackages) || MapUtils.isEmpty(holder.getProperties()) ||
                StringUtils.isBlank(DataSourcePropertiesUtils.getJdbcUrl(holder.getProperties()));
    }

    private static void fillCommonMybatisProperties(TycDataSourceProperties infraDataSourceProperties, Properties propertiesByPrefix) {
        Properties properties = PropertiesUtils.getPropertiesByPrefix(propertiesByPrefix, "mybatis");
        infraDataSourceProperties.setMybatis(addPrefix(properties, "mybatis"));
    }

    /**
     * 获取配置,生成DataSourceHolder
     * @param infraDataSourceProperties
     * @param propertiesByPrefix
     */
    private static void fillDataSourceProperties(Environment environment, TycDataSourceProperties infraDataSourceProperties, Properties propertiesByPrefix) throws ClassNotFoundException {
        Map<String, Properties> dataSourcePropertiesMap = getDataSourceNameList(environment, infraDataSourceProperties, propertiesByPrefix);
        for (Map.Entry<String, Properties> entry : dataSourcePropertiesMap.entrySet()) {
            String dataSourceName = entry.getKey();
            Properties currentDatasourceProperties = entry.getValue();
            if (infraDataSourceProperties.containsKey(entry.getKey())) {
                fillProperties(currentDatasourceProperties, infraDataSourceProperties.getMybatis(), infraDataSourceProperties.get(dataSourceName));
            } else {
                fillNewDataSourceProperties(dataSourceName, currentDatasourceProperties, infraDataSourceProperties);
            }
        }
    }

    private static void fillNewDataSourceProperties(String dataSourceName, Properties currentDatasourceProperties, TycDataSourceProperties infraDataSourceProperties) throws ClassNotFoundException {
        TycDataSourceHolder holder = new TycDataSourceHolder();
        holder.setBeanName(dataSourceName);
        fillCustomHoldParam(holder, currentDatasourceProperties);
        fillMybatisBeanConfiguration(holder, currentDatasourceProperties);
        holder.setProperties(currentDatasourceProperties);
        fillMybatisProperties(holder, currentDatasourceProperties, infraDataSourceProperties.getMybatis());
        infraDataSourceProperties.put(dataSourceName, holder);
    }

    private static void fillMybatisProperties(TycDataSourceHolder holder, Properties currentDatasourceProperties, Properties commonMybatisProperties) {
        MybatisProperties currentDataSourceMybatisProperties = new MybatisProperties();
        currentDataSourceMybatisProperties.setConfiguration(new Configuration());
        currentDataSourceMybatisProperties.setConfigurationProperties(new Properties());
        //改成先合并公共配置
        PropertiesUtils.mergeProperties(commonMybatisProperties, currentDatasourceProperties);
        merge(currentDataSourceMybatisProperties, convert2MybatisProperties(PropertiesUtils.getPropertiesByPrefix(currentDatasourceProperties, "mybatis")));
        holder.setMybatisProperties(currentDataSourceMybatisProperties);
    }


    private static void fillProperties(Properties currentDataSourceProperties, Properties commonMybatisProperties, TycDataSourceHolder holder) throws ClassNotFoundException {
        fillCustomHoldParam(holder, currentDataSourceProperties);
        fillMybatisBeanConfiguration(holder, currentDataSourceProperties);
        holder.setProperties(currentDataSourceProperties);
        fillMybatisProperties(holder, currentDataSourceProperties, commonMybatisProperties);
    }

    private static void fillMybatisBeanConfiguration(TycDataSourceHolder holder, Properties properties) {
        TycDataSourceHolder.MybatisBeanConfiguration mybatisBeanConfiguration = new TycDataSourceHolder.MybatisBeanConfiguration();
        if (StringUtils.isNotBlank(properties.getProperty("mybatis.interceptors"))) {
            mybatisBeanConfiguration.setInterceptors(Arrays.stream(properties.getProperty("mybatis.interceptors").split(",")).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(properties.getProperty("mybatis.typeHandlers"))) {
            mybatisBeanConfiguration.setTypeHandlers(Arrays.stream(properties.getProperty("mybatis.typeHandlers").split(",")).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(properties.getProperty("mybatis.setDatabaseIdProvider"))) {
            mybatisBeanConfiguration.setDatabaseIdProvider(properties.getProperty("mybatis.setDatabaseIdProvider"));
        }
        if (StringUtils.isNotBlank(properties.getProperty("mybatis.languageDrivers"))) {
            mybatisBeanConfiguration.setLanguageDrivers(Arrays.stream(properties.getProperty("mybatis.languageDrivers").split(",")).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(properties.getProperty("mybatis.configurationCustomizers"))) {
            mybatisBeanConfiguration.setConfigurationCustomizers(Arrays.stream(properties.getProperty("mybatis.configurationCustomizers").split(",")).collect(Collectors.toList()));
        }
        holder.setMybatisBeanConfiguration(mybatisBeanConfiguration);
    }

    private static void fillCustomHoldParam(TycDataSourceHolder holder, Properties properties) throws ClassNotFoundException {
        if (StringUtils.isNotBlank(DataSourcePropertiesUtils.getMapperLocation(properties))) {
            holder.setMapperLocation(Lists.newArrayList(DataSourcePropertiesUtils.getMapperLocation(properties)));
        }
        if (CollectionUtils.isNotEmpty(DataSourcePropertiesUtils.getBasePackageClasses(properties))) {
            holder.setBasePackageClasses(DataSourcePropertiesUtils.getBasePackageClasses(properties));
        }
        if (StringUtils.isNotBlank(DataSourcePropertiesUtils.getBasePackages(properties))) {
            holder.setBasePackages(Arrays.stream(DataSourcePropertiesUtils.getBasePackages(properties).split(",")).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(DataSourcePropertiesUtils.getTransactionManager(properties))) {
            holder.setTransactionManagerName(DataSourcePropertiesUtils.getTransactionManager(properties));
        }
    }

    /**
     * 利用Spring自带的BeanWrapper完成属性转对应的原生配置
     * @param properties
     * @return
     */
    private static DataSourceProperties convert2DataSourceProperties(Properties properties) {
        BeanWrapper wrapper = new BeanWrapperImpl(new DataSourceProperties());
        wrapper.setPropertyValues(new MutablePropertyValues(properties), true, true);
        return (DataSourceProperties)wrapper.getWrappedInstance();
    }

    private static MybatisProperties convert2MybatisProperties(Properties properties) {
        MybatisProperties mybatisProperties = new MybatisProperties();
        mybatisProperties.setConfiguration(new Configuration());
        mybatisProperties.setConfigurationProperties(new Properties());
        BeanWrapper wrapper = new BeanWrapperImpl(mybatisProperties);
        PropertiesUtils.mergeProperties(PropertiesUtils.convert2Camel(properties), properties);
        wrapper.setPropertyValues(new MutablePropertyValues(properties), true, true);
        MybatisProperties result = (MybatisProperties)wrapper.getWrappedInstance();
        return result;
    }


    public static Map<String, String> getLocalProperties() {
        return localProperties;
    }

    private static void merge(MybatisProperties mybatisProperties, MybatisProperties datasourceMybatisProperties) {
        mybatisProperties.setConfigLocation(merge(mybatisProperties.getConfigLocation(), datasourceMybatisProperties.getConfigLocation()));
        mybatisProperties.setMapperLocations(merge(mybatisProperties.getMapperLocations(), datasourceMybatisProperties.getMapperLocations()).toArray(new String[0]));
        mybatisProperties.setTypeHandlersPackage(merge(mybatisProperties.getTypeHandlersPackage(), datasourceMybatisProperties.getTypeHandlersPackage()));
        mybatisProperties.setTypeAliasesPackage(merge(mybatisProperties.getTypeAliasesPackage(), datasourceMybatisProperties.getTypeAliasesPackage()));
        mybatisProperties.setTypeAliasesSuperType(merge(mybatisProperties.getTypeAliasesSuperType(), datasourceMybatisProperties.getTypeAliasesSuperType()));
        mybatisProperties.setCheckConfigLocation(merge(mybatisProperties.isCheckConfigLocation(), datasourceMybatisProperties.isCheckConfigLocation()));
        mybatisProperties.setExecutorType(merge(mybatisProperties.getExecutorType(), datasourceMybatisProperties.getExecutorType()));
        mybatisProperties.setDefaultScriptingLanguageDriver(merge(mybatisProperties.getDefaultScriptingLanguageDriver(), datasourceMybatisProperties.getDefaultScriptingLanguageDriver()));
        mybatisProperties.setConfigurationProperties(merge(mybatisProperties.getConfigurationProperties(), datasourceMybatisProperties.getConfigurationProperties()));
        mybatisProperties.setConfiguration(merge(mybatisProperties.getConfiguration(), datasourceMybatisProperties.getConfiguration()));
    }

    private static <T> List<T> merge(T[] source, T[] target) {
        if (ArrayUtils.isEmpty(source) && ArrayUtils.isEmpty(target)) {
            return new ArrayList<>();
        }
        if (ArrayUtils.isEmpty(source)) {
            return Arrays.asList(target);
        }
        List<T> list = Arrays.asList(source);
        list.addAll(Arrays.asList(target));
        return list;
    }

    private static <T> T merge(T source, T target) {
        if (target == null) {
            return source;
        }
        return target;
    }

    private static Properties merge(Properties source, Properties target) {
        if (source == null) {
            return target;
        }
        Properties properties = new Properties();
        PropertiesUtils.mergeProperties(source, properties);
        PropertiesUtils.mergeProperties(target, properties);
        return properties;
    }

    private static Configuration merge(Configuration source, Configuration target) {
        if (source == null) {
            return target;
        }
        Configuration configuration = new Configuration();
        org.springframework.beans.BeanUtils.copyProperties(source, configuration);
        org.springframework.beans.BeanUtils.copyProperties(target, configuration);
        return configuration;
    }

    private static Properties addPrefix(Properties properties, String prefix) {
        Properties result = new Properties();
        properties.forEach((key, value) -> {
            result.put(prefix + "." + key, value);
        });
        return result;
    }
}
