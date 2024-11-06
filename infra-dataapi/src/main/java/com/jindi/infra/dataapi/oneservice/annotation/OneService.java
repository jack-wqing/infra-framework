package com.jindi.infra.dataapi.oneservice.annotation;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OneService {

    /**
     * 项目名
     */
    String project();

    /**
     * 目录
     */
    String folder();
}
