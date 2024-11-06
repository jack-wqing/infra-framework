package com.jindi.infra.dataapi.spacecloud.constant;

import com.jindi.infra.dataapi.spacecloud.param.SpaceCloudParam;

public class SpaceCloudConsts {

    public static final ThreadLocal<SpaceCloudParam> SPACE_CLOUD_PARAM_THREAD_LOCAL = new ThreadLocal<>();

    public static final String ROMA = GraphQLTypeEnums.ROMA.getName();
    public static final String SPACE_CLOUD = GraphQLTypeEnums.SPACE_CLOUD.getName();
    public static final String CLIENT = "client";
    public static final String COUNT = "count";
    public static final String APPLICATION = "application";
    public static final String RESOURCE = "resource";
    public static final String PROJECT = "project";
    public static final String SERVICE = "service";
    public static final String ENDPOINT = "endpoint";
    public static final String RESPONSE_TIME = "ResponseTime";
}
