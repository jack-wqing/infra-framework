package com.jindi.infra.cache.redis.connectionfactory;

import com.google.common.collect.Lists;
import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.tools.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;


@Slf4j
public class TycRedisConnectionFactory implements EnvironmentAware {

    private static final List<String> poolTypeList = Lists.newArrayList("jedis", "lettuce");

    private List<BaseRedisConnectionFactory> iRedisConnectionFactories;

    private Properties commonProperties = new Properties();

    @Override
    public void setEnvironment(Environment environment) {
        commonProperties = PropertiesUtils.getPropertiesByPrefix(environment, "redis.common");
    }

    public TycRedisConnectionFactory(List<BaseRedisConnectionFactory> iRedisConnectionFactories) {
        this.iRedisConnectionFactories = iRedisConnectionFactories;
    }

    public RedisConnectionFactory getConnectionFactory(TycRedisHolder tycRedisHolder) {
        String poolType = getPoolType(tycRedisHolder);
        for (BaseRedisConnectionFactory redisConnectionFactory : iRedisConnectionFactories) {
            if (poolType.contains(redisConnectionFactory.getPoolType())) {
                RedisConnectionFactory connectionFactory = redisConnectionFactory.getConnectionFactory(tycRedisHolder, commonProperties);
                if (connectionFactory != null) {
                    return connectionFactory;
                }
            }
        }
        return null;
    }

    private String getPoolType(TycRedisHolder tycRedisHolder) {
        for (String dsType : poolTypeList) {
            for (Map.Entry<Object, Object> entry : tycRedisHolder.getProperties().entrySet()) {
                if (String.valueOf(entry.getKey()).startsWith(dsType)) {
                    return dsType;
                }
            }
        }
        return "lettuce";
    }
}