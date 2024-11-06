package com.jindi.infra.space.annotation;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Endpoint {

    /**
     * endpoint名
     *
     * @return
     */
    String value();

    /**
     * endpoint类型 SpaceCloud/Roma
     *
     * @return
     */
    String type() default "";
}
