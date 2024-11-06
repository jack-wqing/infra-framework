package com.jindi.infra.cache.redis.connectionfactory.jedis;

import cn.hutool.core.util.ReflectUtil;
import com.jindi.infra.cache.redis.connectionfactory.BaseRedisConnectionFactory;
import com.jindi.infra.cache.redis.exception.TycRedisException;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.tools.util.PropertiesUtils;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Properties;

import static cn.hutool.extra.spring.SpringUtil.getBean;


@Slf4j
public class JedisRedisConnectionFactory extends BaseRedisConnectionFactory {

    @Override
    public String getPoolType() {
        return "jedis";
    }

    @Override
    public RedisConnectionFactory getConnectionFactory(TycRedisHolder tycRedisHolder, Properties commonProperties) {
        RedisProperties redisProperties = getRedisProperties(tycRedisHolder.getProperties(), commonProperties);
        String sentinelConfigurationProviderBeanName = tycRedisHolder.getProperties().getProperty("sentinelConfiguration");
        String clusterConfigurationProviderBeanName = tycRedisHolder.getProperties().getProperty("clusterConfiguration");
        List<String> builderCustomizers = getList(tycRedisHolder.getProperties().getProperty("builderCustomizers"));
        String clientResourcesBeanName = tycRedisHolder.getProperties().getProperty("clientResources");
        JedisRedisConnectionConfiguration lettuceRedisConnectionConfiguration =
                new JedisRedisConnectionConfiguration(redisProperties,
                        getBean(sentinelConfigurationProviderBeanName, RedisSentinelConfiguration.class),
                        getBean(clusterConfigurationProviderBeanName, RedisClusterConfiguration.class),
                        getListBean(builderCustomizers, JedisClientConfigurationBuilderCustomizer.class),
                        getBean(clientResourcesBeanName, ClientResources.class));
        try {
            JedisConnectionFactory connectionFactory = lettuceRedisConnectionConfiguration.redisConnectionFactory();
            connectionFactory.afterPropertiesSet();
            return connectionFactory;
        } catch (Exception e) {
            throw new TycRedisException(e);
        }
    }

    @Override
    protected void fillConnectPool(RedisProperties redisProperties, Properties properties, Properties commonProperties) {
        Properties jedisPrefix = PropertiesUtils.getPropertiesByPrefix(properties, "jedis");
        PropertiesUtils.mergeProperties(commonProperties, jedisPrefix);
        RedisProperties.Jedis jedis = new RedisProperties.Jedis();
        fillPool(jedis, jedisPrefix);
        BeanWrapper wrapper = new BeanWrapperImpl(jedis);
        wrapper.setPropertyValues(new MutablePropertyValues(jedisPrefix), true, true);
        RedisProperties.Jedis result = (RedisProperties.Jedis) wrapper.getWrappedInstance();
        ReflectUtil.setFieldValue(redisProperties, "jedis", result);
        clearByPrefix(properties, "jedis");
    }

}