package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

/**
 * 用于DTO字段上，表示本字段需要被序列化，如何当前DTO存在继承，则需要显示指定该注解 @Author: changbo
 *
 * @date 2021/6/21
 */
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCField {

	/**
	 * 字段序列化的索引位置
	 *
	 * @return
	 */
	int index();

	String name() default "";

	/**
	 * pb类型
	 *
	 * @return
	 */
	String protoType() default "";
}
