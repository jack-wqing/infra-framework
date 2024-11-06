package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

import com.jindi.infra.common.constant.RegionConstant;

/**
 * 标记字段对应的为grpc服务
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCCall {

	String region() default RegionConstant.HUAWEI_REGION;

	int callTimeoutMillis() default 1000;

}
