package com.jindi.infra.cache.redis.connectionfactory.lettuce;

import cn.hutool.core.util.ReflectUtil;
import com.jindi.infra.cache.redis.connectionfactory.BaseRedisConnectionFactory;
import com.jindi.infra.cache.redis.exception.TycRedisException;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.tools.util.PropertiesUtils;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Properties;


@Slf4j
public class LettuceRedisConnectionFactory extends BaseRedisConnectionFactory {

    @Override
    public String getPoolType() {
        return "lettuce";
    }

    @Override
    public RedisConnectionFactory getConnectionFactory(TycRedisHolder tycRedisHolder, Properties commonProperties) {
        RedisProperties redisProperties = getRedisProperties(tycRedisHolder.getProperties(), commonProperties);
        String sentinelConfigurationProviderBeanName = tycRedisHolder.getProperties().getProperty("sentinelConfiguration");
        String clusterConfigurationProviderBeanName = tycRedisHolder.getProperties().getProperty("clusterConfiguration");
        List<String> builderCustomizers = getList(tycRedisHolder.getProperties().getProperty("builderCustomizers"));
        String clientResourcesBeanName = tycRedisHolder.getProperties().getProperty("clientResources");
        LettuceRedisConnectionConfiguration lettuceRedisConnectionConfiguration =
                new LettuceRedisConnectionConfiguration(redisProperties,
                        getBean(sentinelConfigurationProviderBeanName, RedisSentinelConfiguration.class),
                        getBean(clusterConfigurationProviderBeanName, RedisClusterConfiguration.class),
                        getListBean(builderCustomizers, LettuceClientConfigurationBuilderCustomizer.class),
                        getBean(clientResourcesBeanName, ClientResources.class));
        try {
            LettuceConnectionFactory connectionFactory = lettuceRedisConnectionConfiguration.lettuceConnectionFactory();
            connectionFactory.afterPropertiesSet();
            return connectionFactory;
        } catch (Exception e) {
            throw new TycRedisException(e);
        }
    }

    @Override
    protected void fillConnectPool(RedisProperties redisProperties, Properties properties, Properties commonProperties) {
        Properties lettucePrefix = PropertiesUtils.getPropertiesByPrefix(properties, "lettuce");
        PropertiesUtils.mergeProperties(commonProperties, lettucePrefix);
        RedisProperties.Lettuce lettuce = new RedisProperties.Lettuce();
        fillDuration(lettuce, "shutdownTimeout", properties);
        fillPool(lettuce, lettucePrefix);
        fillCluster(lettuce, lettucePrefix);
        BeanWrapper wrapper = new BeanWrapperImpl(lettuce);
        wrapper.setPropertyValues(new MutablePropertyValues(lettucePrefix), true, true);
        RedisProperties.Lettuce result = (RedisProperties.Lettuce) wrapper.getWrappedInstance();
        ReflectUtil.setFieldValue(redisProperties, "lettuce", result);
        clearByPrefix(properties, "lettuce");
    }
}