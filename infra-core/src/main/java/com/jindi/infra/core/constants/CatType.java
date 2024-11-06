package com.jindi.infra.core.constants;

public class CatType {

    /**
     * 方法调用
     */
    public static final String CALL = "Call";

    /**
     * Feign客户端transaction
     */
    public static final String FEIGN_CALL = "FeignCall";

    /**
     * 缓存
     */
    public static final String CACHE = "Cache";

    /**
     * feign服务器端event
     */
    public static final String FEIGN_UPSTREAM = "FeignUpstream";

    /**
     * feign服务器端transaction
     */
    public static final String FEIGN_SERVICE = "FeignService";

    /**
     * rpc服务器
     */
    public static final String RPC_SERVER = "Rpc.Server";
    public static final String RPC_SERVER_APP = "Rpc.Server.app";
    public static final String RPC_SERVER_CLIENT = "Rpc.Server.client";
    public static final String RPC_SERVER_TRACE = "Rpc.Server.Trace";

    /**
     * rpc客户端
     */
    public static final String RPC_CLIENT = "Rpc.Client";
    public static final String RPC_CLIENT_APP = "Rpc.Client.app";
    public static final String RPC_CLIENT_PORT = "Rpc.Client.port";
    public static final String RPC_CLIENT_SERVER = "Rpc.Client.server";

    public static final String CAT_CONTEXT = "cat-context";
    public static final String CLIENT = "client";
    public static final String CLIENT_IP = "client-ip";
}
