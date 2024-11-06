package com.jindi.infra.cache.redis.processor;


import com.jindi.infra.cache.redis.client.TycRedisClient;
import com.jindi.infra.cache.redis.connectionfactory.TycRedisConnectionFactory;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.cache.redis.properties.TycRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;
import java.util.Properties;

@Slf4j
public class TycRedisPostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware, Ordered{

    private TycRedisProperties tycRedisProperties;

    private TycRedisConnectionFactory tycRedisConnectionFactory;

    private BeanFactory beanFactory;

    private RedisSerializer redisSerializer;

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public TycRedisPostProcessor(TycRedisConnectionFactory tycRedisConnectionFactory) {
        this.tycRedisConnectionFactory = tycRedisConnectionFactory;
        this.redisSerializer =  new JdkSerializationRedisSerializer(getClass().getClassLoader());
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        initProperties(beanFactory);

        if (tycRedisProperties != null) {
            for (Map.Entry<String, TycRedisHolder> entry : tycRedisProperties.entrySet()) {
                registerRedisClient(entry.getKey(), entry.getValue(), registry);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private void registerRedisClient(String clientName, TycRedisHolder holder, BeanDefinitionRegistry registry) {
        log.info("=============初始化{}开始===========, properties:{}", clientName, holder.getProperties());
        RedisConnectionFactory connectionFactory = tycRedisConnectionFactory.getConnectionFactory(holder);
        if (connectionFactory == null) {
            return;
        }
        if (!containsBean(clientName + "RedisTemplate")) {
            BeanDefinitionBuilder redisTemplateBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisTemplate.class);
            redisTemplateBuilder.addPropertyValue("connectionFactory", connectionFactory);
            fillSerializer(redisTemplateBuilder, holder);
            registry.registerBeanDefinition(clientName + "RedisTemplate", redisTemplateBuilder.getBeanDefinition());
        }
        if (!containsBean(getStringTemplateName(clientName))) {
            BeanDefinitionBuilder stringRedisTemplateBuilder = BeanDefinitionBuilder.genericBeanDefinition(StringRedisTemplate.class);
            stringRedisTemplateBuilder.addPropertyValue("connectionFactory", connectionFactory);
            registry.registerBeanDefinition(getStringTemplateName(clientName), stringRedisTemplateBuilder.getBeanDefinition());
        }
        if (!containsBean(clientName)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TycRedisClient.class);
            builder.addPropertyValue("redisTemplate", new RuntimeBeanReference(clientName + "RedisTemplate"));
            builder.addPropertyValue("stringRedisTemplate", new RuntimeBeanReference(getStringTemplateName(clientName)));
            builder.addPropertyValue("tycRedisHolder", holder);
            registry.registerBeanDefinition(clientName, builder.getBeanDefinition());
        }
        log.info("=============初始化{}完成===========", clientName);
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

    private String getStringTemplateName(String clientName) {
        return "string" + clientName.substring(0, 1).toUpperCase() + clientName.substring(1) + "RedisTemplate";
    }

    private void initProperties(BeanFactory beanFactory) {
        try {
            tycRedisProperties = (TycRedisProperties) beanFactory.getBean("tycRedisProperties");
        } catch (Exception e) {

        }
    }

    private Boolean containsBean(String beanName) {
        ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)beanFactory;
        return bf.containsBean(beanName);
    }
}
