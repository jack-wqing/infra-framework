package com.jindi.infra.space.properties;

import com.jindi.infra.space.constant.GraphQLTypeEnums;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

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
