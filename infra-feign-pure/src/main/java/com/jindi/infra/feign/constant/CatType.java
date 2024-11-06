package com.jindi.infra.feign.constant;

public class CatType {

    /**
     * Feign客户端transaction
     */
    public static final String FEIGN_CALL = "FeignCall";

    /**
     * feign服务器端event
     */
    public static final String FEIGN_UPSTREAM = "FeignUpstream";

    /**
     * feign服务器端transaction
     */
    public static final String FEIGN_SERVICE = "FeignService";
}
