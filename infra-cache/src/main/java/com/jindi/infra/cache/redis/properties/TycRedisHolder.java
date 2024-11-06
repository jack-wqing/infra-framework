package com.jindi.infra.cache.redis.properties;


import com.jindi.infra.cache.redis.utils.RedisConnectInfoUtils;
import lombok.Data;

import java.util.Properties;

@Data
public class TycRedisHolder {

    private String beanName;

    private Properties properties;

    public TycRedisHolder(String beanName) {
        this.beanName = beanName;
        this.properties = new Properties();
    }

    public TycRedisHolder(String beanName, Properties commonProperties) {
        this.beanName = beanName;
        properties = new Properties();
        commonProperties.forEach((key, value) -> properties.put(key, value));
    }

    public void put(String valueKey, Object value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.put(valueKey, value);
    }

    public String getConnectInfo() {
        return RedisConnectInfoUtils.getConnectInfo(properties.getProperty("host", "unknownHost"),
                properties.getProperty("port", "unknownPort"),
                properties.getProperty("database", "unknownDatabase"));
    }
}
