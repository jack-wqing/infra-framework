package com.jindi.infra.reboot.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.jindi.infra.grpc.client.GrpcClientProxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcClientShutdownConfig implements DisposableBean {

    @Autowired
    private GrpcClientProxy grpcClientProxy;

    @Override
    public void destroy() {
        log.warn("grpc client shutdown");
        try {
            grpcClientProxy.shutdown();
        } catch (Throwable e) {
            log.error("grpc client shutdown error", e);
        }
    }
}
