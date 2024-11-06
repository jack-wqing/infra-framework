package com.jindi.infra.dataapi.spacecloud.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("roma.config")
public class RomaConfigProperties {

    /**
     * 多域名开关
     */
    private Boolean multiDomainSwitch = false;

    /**
     * 多域名
     */
    private String multiDomain = "roma-data-api-dev.services.huawei/v1/api";

    /**
     * 单域名
     */
    private String singleDomain = "http://roma-data-api-dev.jindidata.com/v1/api";

    /**
     * 项目列表
     */
    private String projects = "user";

    /**
     *
     */
    private int readTimeout = 1000;

    /**
     *
     */
    private int writeTimeout = 1000;

    /**
     *
     */
    private int connectTimeout = 1000;

    /**
     *
     */
    private int maxIdleConnections = 10;

    /**
     *
     */
    private int keepAlive = 10 * 60000;
}
