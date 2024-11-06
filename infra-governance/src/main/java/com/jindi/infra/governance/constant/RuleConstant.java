package com.jindi.infra.governance.constant;

public class RuleConstant {

    public static final String TRUE = "true";
    /**
     * nacos metadata 服务http端口
     */
    public static final String SERVER_PORT_KEY = "serverPort";
    /**
     * nacos metadata 服务标签分隔符
     */
    public static final String TAG_SEPARATOR_CHARS = ",";
    /**
     * 路由规则开关
     */
    public static final String NACOS_SWITCH = "nacos.route.rule";
    /**
     * nacos http api，获取实例列表
     */
    public static final String NACOS_URI = "/nacos/v1/ns/instance/list";
    /**
     * 内置nacos连接地址
     */
    public static final String NACOS_DOMAIN_KEY = "nacos.discovery.serverAddr";
    /**
     * nacos本地缓存存储容量
     */
    public static final Integer NACOS_CACHE_MAXIMUMSIZE = 100;
    /**
     * nacos本地缓存过期时间
     */
    public static final Integer NACOS_CACHE_EXPIRE = 5000;

    /**
     * dubbo服务端注册的标签
     */
    public static final String DUBBO_LANE_TAG = "lanetag";

    /**
     * http method 与 url 连接符
     */
    public static final String METHOD_DELIMITER = "#";
}
