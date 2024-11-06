package com.jindi.infra.cache.redis.interceptor;


import com.google.common.util.concurrent.AtomicLongMap;
import com.jindi.infra.cache.redis.client.TycRedisClient;
import com.jindi.infra.cache.redis.connectionfactory.TycRedisConnectionFactory;
import com.jindi.infra.cache.redis.exception.TycRedisException;
import com.jindi.infra.cache.redis.key.Key;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.cache.redis.properties.TycRedisProperties;
import com.jindi.infra.tools.util.BeanUtils;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 用于当Redis出现多次链接异常后重试建立连接
 */
@Slf4j
public class RedisConnectRebuildInterceptor extends RedisInterceptor implements BeanFactoryAware {

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Autowired
    private TycRedisProperties tycRedisProperties;
    @Autowired
    private TycRedisConnectionFactory tycRedisConnectionFactory;

    private BeanFactory beanFactory;

    private AtomicLongMap<String> errorCountMap = AtomicLongMap.create();

    private AtomicLongMap<String> errorCountStartTimeMap = AtomicLongMap.create();

    private AtomicLongMap<String> retryCountMap = AtomicLongMap.create();

    private Map<String, TycRedisHolder> holderMap;

    private RedisSerializer redisSerializer = new JdkSerializationRedisSerializer(getClass().getClassLoader());

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void doError(String opt, Key key, String connection, Throwable e) {
        if (StringUtils.isBlank(connection) || !(e instanceof RedisSystemException
                || e instanceof QueryTimeoutException && e.getCause() instanceof RedisCommandTimeoutException)) {
            return;
        }
        TycRedisHolder holder = getHolder(connection);
        if (holder == null) {
            return;
        }
        long errorStartTime = errorCountStartTimeMap.accumulateAndGet(holder.getBeanName(), System.currentTimeMillis(), (v1, v2) -> v1 <= 0 ? v2 : v2 <= 0 ? v1 : Math.min(v1, v2));
        long errorCount = errorCountMap.incrementAndGet(holder.getBeanName());
        if (errorCount > tycRedisProperties.getConnectErrorCount()
                && (System.currentTimeMillis() - errorStartTime) > tycRedisProperties.getConnectErrorSecond() * 1000
                && retryCountMap.getAndIncrement(holder.getBeanName()) < tycRedisProperties.getConnectErrorRetryCount() ) {
            rebuildConnection(holder);
            clear(holder.getBeanName());
        }
    }

    private void rebuildConnection(TycRedisHolder holder) {
        if (retryCountMap.getAndIncrement(holder.getBeanName()) >= tycRedisProperties.getConnectErrorRetryCount()) {
            log.error("{} redis连接出现异常 && 超过重试次数", holder.getBeanName());
            return;
        }
        String clientName = holder.getBeanName();
        removeExistBean(clientName);
        BeanDefinitionRegistry registry = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        log.info("============={} redis链接出现异常, 开始重连===========, properties:{}", clientName, holder.getProperties());
        RedisConnectionFactory connectionFactory = tycRedisConnectionFactory.getConnectionFactory(holder);
        if (connectionFactory == null) {
            return;
        }
        BeanDefinitionBuilder redisTemplateBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisTemplate.class);
        redisTemplateBuilder.addPropertyValue("connectionFactory", connectionFactory);
        fillSerializer(redisTemplateBuilder, holder);
        registry.registerBeanDefinition(clientName + "RedisTemplate", redisTemplateBuilder.getBeanDefinition());

        BeanDefinitionBuilder stringRedisTemplateBuilder = BeanDefinitionBuilder.genericBeanDefinition(StringRedisTemplate.class);
        stringRedisTemplateBuilder.addPropertyValue("connectionFactory", connectionFactory);
        registry.registerBeanDefinition(getStringTemplateName(clientName), stringRedisTemplateBuilder.getBeanDefinition());

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TycRedisClient.class);
        builder.addPropertyValue("redisTemplate", new RuntimeBeanReference(clientName + "RedisTemplate"));
        builder.addPropertyValue("stringRedisTemplate", new RuntimeBeanReference(getStringTemplateName(clientName)));
        builder.addPropertyValue("tycRedisHolder", holder);
        registry.registerBeanDefinition(clientName, builder.getBeanDefinition());
    }

    private void removeExistBean(String clientName) {
        boolean containsBean = applicationContext.containsBean(clientName);
        //Bean存在先销毁再创建
        if (containsBean) {
            try {
                BeanDefinitionRegistry beanDefinitionRegistry = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
                beanDefinitionRegistry.removeBeanDefinition(clientName);
                beanDefinitionRegistry.removeBeanDefinition(clientName + "RedisTemplate");
                beanDefinitionRegistry.removeBeanDefinition(getStringTemplateName(clientName));
            } catch (Exception e) {
                log.error("移除已经存在的Bean异常");
                throw new TycRedisException("移除已经存在的Bean异常, 重建redis链接" + clientName + "异常", e);
            }
        }
    }

    private void clear(String client) {
        errorCountMap.remove(client);
        errorCountStartTimeMap.remove(client);
    }

    @Override
    public void doAfter(String opt, Key key, String connection) {
        TycRedisHolder holder = getHolder(connection);
        if (holder == null) {
            return;
        }
        clear(holder.getBeanName());
        retryCountMap.remove(holder.getBeanName());
    }

    private String getStringTemplateName(String clientName) {
        return "string" + clientName.substring(0, 1).toUpperCase() + clientName.substring(1) + "RedisTemplate";
    }

    private TycRedisHolder getHolder(String connection) {
        if (StringUtils.isBlank(connection)) {
            return null;
        }
        if (holderMap == null) {
            holderMap = new HashMap<>();
            if (tycRedisProperties != null) {
                for (Map.Entry<String, TycRedisHolder> entry : tycRedisProperties.entrySet()) {
                    holderMap.put(entry.getValue().getConnectInfo(), entry.getValue());
                }
            }
        }
        return holderMap.getOrDefault(connection, null);
    }

    private void fillSerializer(BeanDefinitionBuilder builder, TycRedisHolder holder) {
        builder.addPropertyValue("keySerializer", getSerializer(holder.getProperties(), "keySerializer"));
        builder.addPropertyValue("valueSerializer", getSerializer(holder.getProperties(), "valueSerializer"));
        builder.addPropertyValue("hashKeySerializer", getSerializer(holder.getProperties(), "hashKeySerializer"));
        builder.addPropertyValue("hashValueSerializer", getSerializer(holder.getProperties(), "hashValueSerializer"));
    }

    private RedisSerializer<?> getSerializer(Properties properties, String keySerializer) {
        String value = properties.getProperty(keySerializer);
        if (StringUtils.isBlank(value)) {
            return redisSerializer;
        }
        switch (value) {
            case "string":
                return RedisSerializer.string();
            case "json":
                return RedisSerializer.json();
            case "java":
                return RedisSerializer.java();
            case "byteArray":
                return RedisSerializer.byteArray();
            default:
                RedisSerializer<?> redisSerializer = getRedisSerializerByClass(value);
                return redisSerializer == null ? getRedisSerializerBean(value) : redisSerializer;
        }
    }

    private RedisSerializer<?> getRedisSerializerByClass(String value) {
        try {
            Class<?> clazz = Class.forName(value);
            Object instance = clazz.newInstance();
            if (!(instance instanceof RedisSerializer)) {
                return null;
            }
            return (RedisSerializer<?>) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        }
        return null;
    }

    public RedisSerializer<?> getRedisSerializerBean(String beanName) {
        try {
            return beanFactory.getBean(beanName, RedisSerializer.class);
        } catch (Exception e) {
            log.error("{}序列化器注入失败", beanName, e);
            return redisSerializer;
        }
    }
}
