package com.jindi.infra.feign.constant;

public class FeignConsts {

    public static final String ORIGIN = "origin";

    public static final String FEIGN = "feign";

    public static final String ORIGIN_COOKIE = String.format("%s=%s", ORIGIN, FEIGN);

    public static final String SERVICE_PATH_DELIMITER = ":";
}
