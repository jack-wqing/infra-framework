package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

/**
 * 用于DTO字段上，表示本字段不需要被序列化 @Author: changbo
 *
 * @date 2021/6/21
 */
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCFieldIgnore {
}
