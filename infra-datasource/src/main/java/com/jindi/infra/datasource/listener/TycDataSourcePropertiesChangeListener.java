package com.jindi.infra.datasource.listener;


import cn.hutool.core.util.ReflectUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.jindi.infra.datasource.annotation.TycDataSourceChangeListener;
import com.jindi.infra.datasource.metrics.DataSourceMetricsBinder;
import com.jindi.infra.datasource.properties.TycDataSourceHolder;
import com.jindi.infra.datasource.properties.TycDataSourceProperties;
import com.jindi.infra.datasource.utils.DataSourcePropertiesUtils;
import com.jindi.infra.datasource.utils.DynamicObjectProxy;
import com.jindi.infra.datasource.utils.TycDataSourcePropertiesUtils;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TycDataSourcePropertiesChangeListener implements BeanFactoryAware, EnvironmentAware, ApplicationContextAware {

    @Autowired
    private TycDataSourceProperties infraDataSourceProperties;
    @Autowired(required = false)
    private List<DataSourceMetricsBinder> dataSourceMetricsBinders;

    private final MeterRegistry meterRegistry;

    public TycDataSourcePropertiesChangeListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @TycDataSourceChangeListener
    public void onListener(ConfigChangeEvent changeEvent) {
        if (prefix2DataSource == null) {
            fillPrefix2DataSource();
        }
        Set<String> changedKey = changeEvent.changedKeys();
        Set<String> dataSourceNameList = getDataSourceNameList(changedKey);
        refresh(dataSourceNameList);
    }

    private void fillPrefix2DataSource() {
        prefix2DataSource = new HashMap<>();
        for (TycDataSourceHolder holder : infraDataSourceProperties.values()) {
            if (StringUtils.isEmpty(holder.getPrefix())) {
                prefix2DataSource.put(TycDataSourcePropertiesUtils.SPRING_DATA_SOURCE_PREFIX + holder.getBeanName(), holder.getBeanName());
            } else {
                prefix2DataSource.put(holder.getPrefix(), holder.getBeanName());
            }
        }
    }

    private BeanFactory beanFactory;
    private Environment environment;
    private ApplicationContext applicationContext;
    public static Map<String, String> prefix2DataSource = null;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void refresh(Set<String> dataSourceNameList) {
        if (CollectionUtils.isEmpty(dataSourceNameList)) {
            return;
        }
        TycDataSourceProperties infraDataSourceProperties = TycDataSourcePropertiesUtils.getProperties(applicationContext.getEnvironment(), beanFactory);
        for (String dataSource : dataSourceNameList) {
            TycDataSourceHolder holder = infraDataSourceProperties.get(dataSource);
            if (holder != null) {
                Object oldDataSource = refreshDataSource(dataSource, holder);

                updatePropertiesBean(dataSource, holder);

                close(dataSource, oldDataSource);
            }
        }

    }

    private void updateMetrics(DataSource newDataSource, String dataSource, TycDataSourceHolder holder) {
        if (CollectionUtils.isEmpty(dataSourceMetricsBinders)) {
            return;
        }
        try {
            for (Meter meter : meterRegistry.getMeters()) {
                if (!(meter instanceof Counter)) {
                    continue;
                }
                if (!meter.getId().getName().equals("tyc.datasource.relation")) {
                    continue;
                }
                if (!meter.getId().getTag("datasource").equals(dataSource)) {
                    continue;
                }
                meterRegistry.remove(meter);
                for (DataSourceMetricsBinder dataSourceMetricsBinder : dataSourceMetricsBinders) {
                    dataSourceMetricsBinder.binDataSourceMetrics(newDataSource, meterRegistry);
                }
                log.info("[new]{} DataSourceMetrics变更完成", dataSource);
            }
        } catch (Exception e) {
            log.error("DataSourceMetrics变更出现异常, dataSource:{}", dataSource, e);
        }
    }

    private void updatePropertiesBean(String dataSource, TycDataSourceHolder holder) {
        infraDataSourceProperties.put(dataSource, holder);
    }

    private void refreshCatMybatisPlugin(String dataSource, TycDataSourceHolder holder) {
        try {
            log.info("[new]{} CatMybatisInterceptor开始变更", dataSource);
            String url = DataSourcePropertiesUtils.getJdbcUrl(holder.getProperties());
            Object bean = beanFactory.getBean(dataSource + "SqlSessionFactory");
            if (!(bean instanceof SqlSessionFactory)) {
                log.error("非SqlSessionFactory, 无法动态更新, dataSource:{}", dataSource);
                return;
            }
            SqlSessionFactory proxy = (SqlSessionFactory) bean;
            updateInterceptor(proxy.getConfiguration().getInterceptors(), url);
            log.info("[new]{} CatMybatisInterceptor变更完成", dataSource);
        } catch (Exception e) {
            log.error("不存在该数据源, 无法进行更新, dataSource:{}", dataSource);
        }
    }

    private void updateInterceptor(List<Interceptor> interceptors, String url) {
        if (CollectionUtils.isEmpty(interceptors)) {
            return;
        }
        List<Interceptor> oldInterceptor = interceptors.stream().filter(interceptor ->
                interceptor.getClass().getSimpleName().contains("CatMybatisInterceptor")).collect(Collectors.toList());
        for (Interceptor interceptor : oldInterceptor) {
            ReflectUtil.setFieldValue(interceptor, "uri", url);
        }
    }

    private Object refreshDataSource(String dataSource, TycDataSourceHolder holder) {
        try {
            log.info("[new]{} 数据源开始创建", dataSource);
            DataSource newDataSource = holder.getDataSource();
            log.info("[new]{} 数据源创建成功", dataSource);
            Object bean = beanFactory.getBean(dataSource);
            Object proxy = getTarget(bean);
            if (!(proxy instanceof DynamicObjectProxy)) {
                log.error("非动态数据源, 无法进行更新, dataSource:{}", dataSource);
                return null;
            }
            Object oldDataSource = ((DynamicObjectProxy) proxy).getAndSetNewDelegate(newDataSource);
            refreshCatMybatisPlugin(dataSource, holder);
            updateMetrics(newDataSource, dataSource, holder);
            return oldDataSource;
        } catch (Exception e) {
            log.error("{}数据源实时更新异常, dataSource:{}", dataSource, e);
        }
        return null;
    }

    private void close(String dataSource, Object oldDataSource) {
        if (oldDataSource == null) {
            return;
        }
        try {
            Thread.sleep(10000);
            Method closeMethod = oldDataSource.getClass().getDeclaredMethod("close");
            closeMethod.invoke(oldDataSource);
            return;
        } catch (Exception e) {
            log.debug("通过反射关闭数据源失败", e);
        }
        try {
            if (oldDataSource instanceof HikariDataSource) {
                ((HikariDataSource) oldDataSource).close();
            } else if (oldDataSource instanceof DruidDataSource) {
                ((DruidDataSource) oldDataSource).close();
            } else {
                ((DataSource) oldDataSource).getConnection().close();
            }
        } catch (Exception e) {
            log.error("关闭数据源失败, dataSource:{}", dataSource, e);
        }
    }

    private Set<String> getDataSourceNameList(Set<String> changedKeys) {
        if (CollectionUtils.isEmpty(changedKeys)) {
            return new HashSet<>();
        }
        List<String> nameList = new ArrayList<>();
        for (String changedKey : changedKeys) {
            String dataSource = getDataSource(changedKey);
            nameList.add(dataSource);
        }
        return nameList.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static String getDataSource(String key) {
        for (Map.Entry<String, String> entry : prefix2DataSource.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public static Object getTarget(Object proxy) throws Exception {
        if (!Proxy.isProxyClass(proxy.getClass())) {
            return proxy;
        }
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        return h.get(proxy);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
