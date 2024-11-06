package com.jindi.infra.core.annotation;

import java.lang.annotation.*;

/**
 * Grpc接口
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCInterface {
}
