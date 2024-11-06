package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

/**
 * 用于DTO类上，表示当前类需要被序列化 @Author: changbo
 *
 * @date 2021/6/21
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCModel {
}
