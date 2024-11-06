package com.jindi.infra.cache.redis.connectionfactory;

import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.Lists;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.tools.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


public abstract class BaseRedisConnectionFactory implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public abstract String getPoolType();

    public abstract RedisConnectionFactory getConnectionFactory(TycRedisHolder tycRedisHolder, Properties commonProperties);

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    protected  <T> T getBean(String beanName, Class<T> tClass) {
        try {
            return beanFactory.getBean(beanName, tClass);
        } catch (Exception e) {
            return null;
        }
    }

    protected  <T> List<T> getListBean(List<String> beanNames, Class<T> tClass) {
        if (CollectionUtils.isEmpty(beanNames)) {
            return new ArrayList<>();
        }
        try {
            List<T> list = new ArrayList<>();
            for (String beanName : beanNames) {
                list.add(getBean(beanName, tClass));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    protected List<String> getList(String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(",")).map(String::trim).collect(Collectors.toList());
    }

    protected RedisProperties getRedisProperties(Properties properties, Properties commonProperties) {
        RedisProperties redisProperties = new RedisProperties();
        PropertiesUtils.mergeProperties(PropertiesUtils.convert2Camel(properties), properties);
        fillDuration(redisProperties, "timeout", commonProperties.getProperty("timeout"));
        fillDuration(redisProperties, "timeout", properties);
        fillSentinel(redisProperties, properties);
        fillCluster(redisProperties, properties);
        fillConnectPool(redisProperties, properties, commonProperties);
        BeanWrapper wrapper = new BeanWrapperImpl(redisProperties);
        wrapper.setPropertyValues(new MutablePropertyValues(properties), true, true);
        RedisProperties result = (RedisProperties) wrapper.getWrappedInstance();
        return result;
    }

    protected abstract void fillConnectPool(RedisProperties redisProperties, Properties properties, Properties commonProperties);

    protected void fillCluster(RedisProperties.Lettuce lettuce, Properties properties) {
        Properties clusterPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "cluster");
        if (CollectionUtils.isEmpty(clusterPrefix)) {
            return;
        }
        RedisProperties.Lettuce.Cluster cluster = new RedisProperties.Lettuce.Cluster();
        fillRefresh(cluster, clusterPrefix);
        BeanWrapper wrapper = new BeanWrapperImpl(cluster);
        wrapper.setPropertyValues(new MutablePropertyValues(clusterPrefix), true, true);
        RedisProperties.Cluster result = (RedisProperties.Cluster) wrapper.getWrappedInstance();
        ReflectUtil.setFieldValue(lettuce, "cluster", result);
        clearByPrefix(properties, "cluster");
    }

    protected void fillRefresh(RedisProperties.Lettuce.Cluster cluster, Properties properties) {
        Properties refreshPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "refresh");
        if (CollectionUtils.isEmpty(refreshPrefix)) {
            return;
        }
        RedisProperties.Lettuce.Cluster.Refresh refresh = new RedisProperties.Lettuce.Cluster.Refresh();
        fillDuration(refresh, "period", refreshPrefix);
        BeanWrapper wrapper = new BeanWrapperImpl(refresh);
        wrapper.setPropertyValues(new MutablePropertyValues(refreshPrefix), true, true);
        RedisProperties.Cluster result = (RedisProperties.Cluster) wrapper.getWrappedInstance();
        ReflectUtil.setFieldValue(cluster, "refresh", result);
    }

    protected void fillPool(RedisProperties.Lettuce lettuce, Properties properties) {
        Properties poolPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "pool");
        if (CollectionUtils.isEmpty(poolPrefix)) {
            return;
        }
        RedisProperties.Pool pool = new RedisProperties.Pool();
        fillDurations(pool, Lists.newArrayList("maxWait", "timeBetweenEvictionRuns"), poolPrefix);
        BeanWrapper wrapper = new BeanWrapperImpl(pool);
        wrapper.setPropertyValues(new MutablePropertyValues(poolPrefix), true, true);
        RedisProperties.Pool result = (RedisProperties.Pool) wrapper.getWrappedInstance();
        ReflectUtil.setFieldValue(lettuce, "pool", result);
        clearByPrefix(properties, "pool");
    }

    protected void clearByPrefix(Properties properties, String prefix) {
        List<String> keyList = properties.keySet().stream().map(String::valueOf).filter(key -> key.startsWith(prefix)).collect(Collectors.toList());
        keyList.forEach(properties::remove);
    }

    protected void fillPool(RedisProperties.Jedis jedis, Properties properties) {
        Properties poolPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "pool");
        if (CollectionUtils.isEmpty(poolPrefix)) {
            return;
        }
        RedisProperties.Pool pool = new RedisProperties.Pool();
        fillDurations(pool, Lists.newArrayList("maxWait", "timeBetweenEvictionRuns"), poolPrefix);
        BeanWrapper wrapper = new BeanWrapperImpl(pool);
        wrapper.setPropertyValues(new MutablePropertyValues(poolPrefix), true, true);
        RedisProperties.Pool result = (RedisProperties.Pool) wrapper.getWrappedInstance();
        ReflectUtil.setFieldValue(jedis, "pool", result);
        clearByPrefix(properties, "pool");
    }

    protected void fillCluster(RedisProperties redisProperties, Properties properties) {
        Properties clusterPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "cluster");
        if (CollectionUtils.isEmpty(clusterPrefix)) {
            return;
        }
        BeanWrapper wrapper = new BeanWrapperImpl(new RedisProperties.Cluster());
        wrapper.setPropertyValues(new MutablePropertyValues(clusterPrefix), true, true);
        RedisProperties.Cluster result = (RedisProperties.Cluster) wrapper.getWrappedInstance();
        redisProperties.setCluster(result);
        clearByPrefix(properties, "cluster");
    }

    protected void fillSentinel(RedisProperties redisProperties, Properties properties) {
        Properties sentinelPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "sentinel");
        if (CollectionUtils.isEmpty(sentinelPrefix)) {
            return;
        }
        BeanWrapper wrapper = new BeanWrapperImpl(new RedisProperties.Sentinel());
        wrapper.setPropertyValues(new MutablePropertyValues(sentinelPrefix), true, true);
        RedisProperties.Sentinel result = (RedisProperties.Sentinel) wrapper.getWrappedInstance();
        redisProperties.setSentinel(result);
        clearByPrefix(properties, "sentinel");
    }

    protected void fillDuration(Object obj, String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (StringUtils.isBlank(value)) {
            return;
        }
        ReflectUtil.setFieldValue(obj, key, Duration.ofMillis(Long.parseLong(value)));
    }

    protected void fillDuration(Object obj, String key, Properties properties) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return;
        }
        ReflectUtil.setFieldValue(obj, key, Duration.ofMillis(Long.parseLong(value)));
        properties.remove(key);
    }

    protected void fillDurations(Object obj, List<String> keyList, Properties properties) {
        for (String key : keyList) {
            fillDuration(obj, key, properties);
        }
    }
}