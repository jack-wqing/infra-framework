package com.jindi.infra.dataapi.oneservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

import java.util.Map;

@Data
@ConfigurationProperties("oneservice.config")
public class OneServiceConfigProperties {

    // okhttp3
    private int readTimeout = 1000; // 读取超时时间(okHttp3)

    private int writeTimeout = 1000; // 写入超时时间(okHttp3)

    private int connectTimeout = 1000; // 连接超时时间(okHttp3 + httpcomponents)

    private int maxIdleConnections = 200; // 连接池的最大空闲连接数(okHttp3)

    // httpcomponents
    private int maxConnections = 500;  // 连接池的最大连接数

    private int maxPerRouteConnections = 30; // 每个路由的最大连接数

    private int connectionRequestTimeout = 100; // 连接获取超时时间

    private int socketTimeout = 1000; // 读取超时时间

    private int idleConnectTime = 5 * 60000; // 空闲连接存活时间
    /**
     *
     * oneservice.config.project.executor1 -> appName#url
     */
    private Map<String, String> project;

    /**
     * oneservice.config.url.executor1 -> http://executor-1.services.huawei/api
     */
    private Map<String, String> url;

    /**
     * oneservice.config.discovery.executor1 -> 注册中心服务名
     */
    private Map<String, String> discovery;

}
