package com.jindi.infra.dataapi.oneservice.annotation;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneServiceApi {

    /**
     * api名称
     */
    String api();

    /**
     * api版本
     */
    String version() default "v1";
}
