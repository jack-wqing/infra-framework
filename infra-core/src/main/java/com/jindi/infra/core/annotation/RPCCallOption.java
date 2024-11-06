package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCCallOption {

	/**
	 * 捕获异常列表
	 *
	 * @return
	 */
	Class<?>[] exception() default Throwable.class;
}
