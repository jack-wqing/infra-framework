package com.jindi.infra.dataapi.spacecloud.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.jindi.infra.dataapi.spacecloud.constant.GraphQLTypeEnums;

import lombok.Data;

@Data
@ConfigurationProperties("spacecloud.config")
public class SpaceCloudConfigProperties {

    /**
     * 默认GraphQL类型
     */
    private String defaultGraphQLType = GraphQLTypeEnums.SPACE_CLOUD.getName();

    /**
     * spacecloud.config.project.business-risk -> project#service#endpoint
     */
    private Map<String, String> project;

    /**
     * spacecloud.config.url.business-risk -> http://spaceCloudUrl/v1/api
     */
    private Map<String, String> url;

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
