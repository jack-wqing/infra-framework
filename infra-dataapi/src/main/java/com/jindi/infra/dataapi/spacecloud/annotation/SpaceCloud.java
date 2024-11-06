package com.jindi.infra.dataapi.spacecloud.annotation;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpaceCloud {

    /**
     * 项目名
     *
     * @return
     */
    String project();

    /**
     * 服务名
     *
     * @return
     */
    String service();
}
