package com.jindi.infra.reboot.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.jindi.infra.grpc.server.GrpcServiceProxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServerShutdownConfig implements DisposableBean {

    @Autowired
    private GrpcServiceProxy grpcServiceProxy;

    @Override
    public void destroy() {
        log.warn("grpc server shutdown");
        try {
            grpcServiceProxy.shutdown();
        } catch (Throwable e) {
            log.error("grpc server shutdown error", e);
        }
    }
}
