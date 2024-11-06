package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

/**
 * 用于DTO类上，表示当前类的所有字段都需要被序列化，字段顺序默认按照编码顺序，
 * 如果该DTO存在继承，则此注解失效，需使用RPCField显示指定顺序 @Author: changbo
 *
 * @date 2021/6/21
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCAllFields {
}
