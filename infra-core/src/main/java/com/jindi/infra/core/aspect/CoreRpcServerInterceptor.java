package com.jindi.infra.core.aspect;


public interface CoreRpcServerInterceptor {

    void before(String className, String methodName, Object... params);

    void after(String className, String methodName, Object response, Object... params);

    int getOrder();
}
