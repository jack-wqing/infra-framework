package com.jindi.infra.cache.redis.properties;


import lombok.Data;

import java.util.Hashtable;
import java.util.Properties;

@Data
public class TycRedisProperties extends Hashtable<String, TycRedisHolder> {

    private Properties commonProperties;
    /**
     * redis连接异常进行重试的阈值 (链接异常 > errorCount次 && 链接异常持续时间 > errorTime 次会进行重新建连)
     */
    public Long getConnectErrorCount() {
        return Long.parseLong(commonProperties.getProperty("connectErrorCount", "10"));
    }

    /**
     * redis连接发生异常>errorTime(ms)后进行重试阈值 (链接异常 > errorCount次 && 链接异常持续时间 > errorTime 次会进行重新建连)
     */
    public Long getConnectErrorSecond() {
        return Long.parseLong(commonProperties.getProperty("connectErrorSecond", "10"));
    }

    /**
     * redis异常后,重新建连的次数限制;
     */
    public Long getConnectErrorRetryCount() {
        return Long.parseLong(commonProperties.getProperty("connectErrorRetryCount", "5"));
    }

    public void setCommonProperties(Properties commonProperties) {
        this.commonProperties = commonProperties;
    }

    public Properties getCommonProperties() {
        return commonProperties;
    }
}
