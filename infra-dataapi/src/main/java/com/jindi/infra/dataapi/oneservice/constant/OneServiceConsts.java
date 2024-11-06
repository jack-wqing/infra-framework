package com.jindi.infra.dataapi.oneservice.constant;

import com.jindi.infra.dataapi.oneservice.param.OneServiceParam;

public class OneServiceConsts {

    public static final ThreadLocal<OneServiceParam> ONE_SERVICE_PARAM_THREAD_LOCAL = new ThreadLocal<>();

    public static final String ONE_SERVICE = "OneService";
    public static final String CLIENT = "client";
    public static final String COUNT = "count";
    public static final String APPLICATION = "application";
    public static final String RESOURCE = "resource";
    public static final String PROJECT = "project";
    public static final String FOLDER = "folder";
    public static final String API = "api";
    public static final String VERSION = "version";
    public static final String RESPONSE_TIME = "ResponseTime";
    public static final String X_AUTH_TOKEN = "X-Auth-Token";
    public static final String ONE_SERVICE_NAMESPACE = "TYC-COMMON.oneservice";

}
