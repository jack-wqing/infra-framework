package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

/**
 * Grpc方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCMethod {
}
