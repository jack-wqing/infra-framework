package com.jindi.infra.datasource.processor;

import com.jindi.infra.datasource.properties.TycDataSourceHolder;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import com.jindi.infra.tools.util.BeanUtils;
import com.jindi.infra.datasource.utils.DataSourcePropertiesUtils;
import com.jindi.infra.metrics.cat.interceptor.CatMybatisInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TycMybatisBeanFactoryPostprocessor implements BeanFactoryPostProcessor, ResourceLoaderAware, Ordered, BeanFactoryAware {

    private ResourceLoader resourceLoader;
    private BeanFactory beanFactory;

    private static TycDataSourceProperties infraDataSourceProperties;
    private static PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    @Autowired
    private ObjectProvider<List<ConfigurationCustomizer>>  configurationCustomizers;

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 2000;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        fillInfraDataSourceProperties(beanFactory);
        String[] beanNames = BeanUtils.getBeanNamesForType(beanFactory, DataSource.class);
        for (String beanName : beanNames) {
            if (!TycDataSourceBeanFactoryPostProcessor.isInfraDataSource(beanName)) {
                continue;
            }
            try {
                DataSource dataSource = beanFactory.getBean(beanName, DataSource.class);
                SqlSessionFactory sqlSessionFactory = registerSqlSessionFactory(beanFactory, beanName, dataSource);
                SqlSessionTemplate sqlSessionTemplate = registerSqlSessionTemplate(beanFactory, beanName, sqlSessionFactory);
                registerJdbcTemplate(beanFactory, beanName, dataSource);
                registerTransactionManager(beanFactory, beanName, dataSource);
            } catch (Exception e) {
                log.error("数据源初始化失败, dataSourceName:{}", beanName, e);
            }
        }
    }


    private void fillInfraDataSourceProperties(BeanFactory beanFactory) {
        try {
            infraDataSourceProperties = beanFactory.getBean(TycDataSourceProperties.class);
        } catch (Exception e) {
            // 无额外配置
        }
    }

    public SqlSessionFactory registerSqlSessionFactory(BeanFactory beanFactory, String dataSourceName, DataSource dataSource) throws Exception {
        MybatisProperties mybatisProperties = genMybatisProperties(dataSourceName, infraDataSourceProperties);
        SqlSessionFactory sqlSessionFactory = genSqlSessionFactory(dataSource, mybatisProperties, beanFactory, infraDataSourceProperties.get(dataSourceName));
        return BeanUtils.registerIfAbsent(beanFactory, dataSourceName + "SqlSessionFactory", sqlSessionFactory);
    }

    private MybatisProperties genMybatisProperties(String dataSourceName, TycDataSourceProperties infraDataSourceProperties) {
        return infraDataSourceProperties.get(dataSourceName).getMybatisProperties();
//        MybatisProperties mybatisProperties = new MybatisProperties();
//        fillCommonProperties(mybatisProperties, infraDataSourceProperties);
//        fillCurrentDataSourceProperties(mybatisProperties, infraDataSourceProperties.get(dataSourceName).getMybatisProperties());
//        return mybatisProperties;
    }



    private void fillCommonProperties(MybatisProperties mybatisProperties, TycDataSourceProperties infraMySQLProperties) {
        org.springframework.beans.BeanUtils.copyProperties(infraMySQLProperties.getMybatis(), mybatisProperties);
    }

    private SqlSessionTemplate registerSqlSessionTemplate(BeanFactory beanFactory, String dataSourceName, SqlSessionFactory sqlSessionFactory) {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        return BeanUtils.registerIfAbsent(beanFactory, dataSourceName + "SqlSessionTemplate", sqlSessionTemplate);
    }

    private void registerJdbcTemplate(BeanFactory beanFactory, String dataSourceName, DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        BeanUtils.registerIfAbsent(beanFactory, dataSourceName + "JdbcTemplate", jdbcTemplate);
    }

    private void registerTransactionManager(BeanFactory beanFactory, String dataSourceName, DataSource dataSource) {
        if (!infraDataSourceProperties.containsKey(dataSourceName)) {
            return;
        }
        TycDataSourceHolder infraDataSourceHolder = infraDataSourceProperties.get(dataSourceName);
        if (StringUtils.isBlank(infraDataSourceHolder.getTransactionManagerName())) {
            return;
        }
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        BeanUtils.registerIfAbsent(beanFactory, infraDataSourceHolder.getTransactionManagerName(), dataSourceTransactionManager);
    }

    public SqlSessionFactory genSqlSessionFactory(DataSource dataSource, MybatisProperties mybatisProperties, BeanFactory beanFactory, TycDataSourceHolder infraDataSourceHolder) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        fillConfigLocation(mybatisProperties, factory);
        applyConfiguration(factory, mybatisProperties);
        fillConfigurationProperties(mybatisProperties, factory);
        fillPlugins(factory, beanFactory, infraDataSourceHolder);
        fillDatabaseIdProvider(factory, infraDataSourceHolder);
        fillTypeAlias(mybatisProperties, factory);
        fillTypeHandlers(mybatisProperties, factory, infraDataSourceHolder);
        fillLanguageDriver(mybatisProperties, factory, infraDataSourceHolder);
        fillMapperLocation(infraDataSourceHolder, factory);

        return factory.getObject();
    }

    private void fillMapperLocation(TycDataSourceHolder infraDataSourceHolder, SqlSessionFactoryBean factory) throws IOException {
        if (!CollectionUtils.isEmpty(infraDataSourceHolder.getMapperLocation())) {
            List<Resource> resources = new ArrayList<>();
            for (String mapperLocations : infraDataSourceHolder.getMapperLocation()) {
                for (String mapperLocation : mapperLocations.split(",")) {
                    resources.addAll(Arrays.asList(resourcePatternResolver.getResources(mapperLocation)));
                }
            }
            factory.setMapperLocations(resources.toArray(new Resource[0]));
        }
    }

    private void fillLanguageDriver(MybatisProperties mybatisProperties, SqlSessionFactoryBean factory, TycDataSourceHolder infraDataSourceHolder) {
        Set<String> factoryPropertyNames = (Set) Stream.of((new BeanWrapperImpl(SqlSessionFactoryBean.class)).getPropertyDescriptors()).map(FeatureDescriptor::getName).collect(Collectors.toSet());
        List<LanguageDriver> languageDrivers = new ArrayList<>();
        for (String languageDriverName : infraDataSourceHolder.getMybatisBeanConfiguration().getLanguageDrivers()) {
            try {
                LanguageDriver languageDriver = beanFactory.getBean(languageDriverName, LanguageDriver.class);
                languageDrivers.add(languageDriver);
            } catch (Exception e) {
                log.debug("[{}]的[{}]languageDriver 不存在", infraDataSourceHolder.getBeanName(), languageDriverName);
            }
        }
        Class<? extends LanguageDriver> defaultLanguageDriver = null;
        if (mybatisProperties == null) {
            if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !CollectionUtils.isEmpty(languageDrivers)) {
                factory.setScriptingLanguageDrivers(languageDrivers.toArray(new LanguageDriver[0]));
                if (defaultLanguageDriver == null && languageDrivers.size() == 1) {
                    defaultLanguageDriver = languageDrivers.get(0).getClass();
                }
            }
        } else {
            defaultLanguageDriver = mybatisProperties.getDefaultScriptingLanguageDriver();
            if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !CollectionUtils.isEmpty(languageDrivers)) {
                factory.setScriptingLanguageDrivers(languageDrivers.toArray(new LanguageDriver[0]));
                if (defaultLanguageDriver == null && languageDrivers.size() == 1) {
                    defaultLanguageDriver = languageDrivers.get(0).getClass();
                }
            }
        }

        if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
            factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
        }
    }

    private void fillTypeHandlers(MybatisProperties mybatisProperties, SqlSessionFactoryBean factory, TycDataSourceHolder infraDataSourceHolder) {
        if (mybatisProperties != null && org.springframework.util.StringUtils.hasLength(mybatisProperties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(mybatisProperties.getTypeHandlersPackage());
        }

        if (CollectionUtils.isEmpty(infraDataSourceHolder.getMybatisBeanConfiguration().getTypeHandlers())) {
            return;
        }
        List<TypeHandler<?>> typeHandlers = new ArrayList<>();
        for (String typeHandlerName : infraDataSourceHolder.getMybatisBeanConfiguration().getTypeHandlers()) {
            try {
                TypeHandler typeHandler = beanFactory.getBean(typeHandlerName, TypeHandler.class);
                typeHandlers.add(typeHandler);
            } catch (Exception e) {
                log.debug("[{}]的[{}]typeHandler 不存在", infraDataSourceHolder.getBeanName(), typeHandlerName);
            }
        }

        if (!CollectionUtils.isEmpty(typeHandlers)) {
            factory.setTypeHandlers(typeHandlers.toArray(new TypeHandler[0]));
        }
    }

    private void fillTypeAlias(MybatisProperties mybatisProperties, SqlSessionFactoryBean factory) {
        if (mybatisProperties != null && org.springframework.util.StringUtils.hasLength(mybatisProperties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        }

        if (mybatisProperties != null && mybatisProperties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(mybatisProperties.getTypeAliasesSuperType());
        }
    }

    private void fillDatabaseIdProvider(SqlSessionFactoryBean factory, TycDataSourceHolder infraDataSourceHolder) {
        if (infraDataSourceHolder.getMybatisBeanConfiguration().getDatabaseIdProvider() == null) {
            return;
        }
        try {
            DatabaseIdProvider provider = beanFactory.getBean(infraDataSourceHolder.getMybatisBeanConfiguration().getDatabaseIdProvider(), DatabaseIdProvider.class);
            factory.setDatabaseIdProvider(provider);
        } catch (Exception e) {
            log.debug("{}的{}databaseIdProvider 不存在", infraDataSourceHolder.getBeanName(), infraDataSourceHolder.getMybatisBeanConfiguration().getDatabaseIdProvider());
        }
    }

    private void fillConfigurationProperties(MybatisProperties mybatisProperties, SqlSessionFactoryBean factory) {
        if (mybatisProperties != null && mybatisProperties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(mybatisProperties.getConfigurationProperties());
        }
    }

    private void fillConfigLocation(MybatisProperties mybatisProperties, SqlSessionFactoryBean factory) {
        if (mybatisProperties != null && org.springframework.util.StringUtils.hasText(mybatisProperties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(mybatisProperties.getConfigLocation()));
        }
    }

    private void applyConfiguration(SqlSessionFactoryBean factory, MybatisProperties mybatisProperties) {
        org.apache.ibatis.session.Configuration configuration = null;
        if (mybatisProperties == null) {
            configuration = new org.apache.ibatis.session.Configuration();
        } else {
            configuration = mybatisProperties.getConfiguration();
            if (configuration == null && !org.springframework.util.StringUtils.hasText(mybatisProperties.getConfigLocation())) {
                configuration = new org.apache.ibatis.session.Configuration();
            }
        }

        List<ConfigurationCustomizer> customizers = configurationCustomizers == null ? new ArrayList<>() : configurationCustomizers.getIfAvailable();
        if (configuration != null && !CollectionUtils.isEmpty(customizers)) {
            for (ConfigurationCustomizer customizer : customizers) {
                customizer.customize(configuration);
            }
        }

        factory.setConfiguration(configuration);
    }

    private void fillPlugins(SqlSessionFactoryBean factory, BeanFactory beanFactory, TycDataSourceHolder infraDataSourceHolder) {
        List<String> interceptorNames = infraDataSourceHolder.getMybatisBeanConfiguration().getInterceptors();
        List<Interceptor> interceptors = new ArrayList<>();
        fillDefaultPlugins(interceptors, infraDataSourceHolder);
        fillClassPlugins(interceptors, interceptorNames);
        fillBeanPlugins(interceptors, beanFactory, interceptorNames);
        interceptors = clearDuplicate(infraDataSourceHolder, interceptors);
        factory.setPlugins(interceptors.toArray(new Interceptor[interceptors.size()]));
    }

    private List<Interceptor> clearDuplicate(TycDataSourceHolder infraDataSourceHolder, List<Interceptor> interceptors) {
        Map<String, Interceptor> interceptorMap = interceptors.stream().collect(Collectors.toMap(ClassUtils::getSimpleName, Function.identity(), (v1, v2) -> v1));
        if (interceptorMap.size() < interceptors.size()) {
            log.error("{}mybatis插件存在冲突", infraDataSourceHolder.getBeanName());
            return new ArrayList<>(interceptorMap.values());
        }
        return interceptors;
    }

    private void fillDefaultPlugins(List<Interceptor> interceptors, TycDataSourceHolder infraDataSourceHolder) {
        interceptors.add(new CatMybatisInterceptor(DataSourcePropertiesUtils.getJdbcUrl(infraDataSourceHolder.getProperties())));
    }

    private void fillBeanPlugins(List<Interceptor> interceptors, BeanFactory beanFactory, List<String> pluginList) {
        if (CollectionUtils.isEmpty(pluginList)) {
            return;
        }
        for (String pluginBean : pluginList) {
            try {
                Object bean = beanFactory.getBean(pluginBean);
                if (bean instanceof Interceptor) {
                    interceptors.add((Interceptor) bean);
                }
            } catch (Exception e) {
                //do noting
            }
        }
    }

    private void fillClassPlugins(List<Interceptor> interceptors, List<String> pluginList) {
        if (CollectionUtils.isEmpty(pluginList)) {
            return;
        }
        for (String plugin : pluginList) {
            try {
                Class<?> clazz = Class.forName(plugin);
                Object instance = clazz.newInstance();
                if (!(instance instanceof Interceptor)) {
                    continue;
                }
                interceptors.add((Interceptor) instance);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            }
        }
    }


}
