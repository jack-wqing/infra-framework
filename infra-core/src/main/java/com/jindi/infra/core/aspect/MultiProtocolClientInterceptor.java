package com.jindi.infra.core.aspect;


import java.util.Map;

public interface MultiProtocolClientInterceptor {

    default void before(String method, Map<String, String> extHeaders) {

    }

    default void after(String method) {

    }

}
