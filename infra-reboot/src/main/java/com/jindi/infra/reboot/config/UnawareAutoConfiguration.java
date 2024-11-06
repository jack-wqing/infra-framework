package com.jindi.infra.reboot.config;

import com.jindi.infra.grpc.client.GrpcClientProxy;
import com.jindi.infra.grpc.server.GrpcServiceProxy;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.reboot.controller.RebootController;
import com.jindi.infra.reboot.dubbo.DubboDestory;
import com.jindi.infra.reboot.eureka.EurekaDestory;
import com.jindi.infra.reboot.listener.UnawareBootListener;

@Configuration
public class UnawareAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public UnawareBootListener unawareBootListener() {
        return new UnawareBootListener();
    }

    @ConditionalOnClass(name = "com.alibaba.dubbo.config.ProtocolConfig")
    @ConditionalOnMissingClass("org.apache.dubbo.config.ProtocolConfig")
    @Bean
    public DubboDestory dubboDestory() {
        return new DubboDestory();
    }

    @ConditionalOnClass(name = {"org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration", "com.netflix.discovery.EurekaClient"})
    @Bean
    public EurekaDestory eurekaDestory() {
        return new EurekaDestory();
    }

    @ConditionalOnWebApplication
    @ConditionalOnMissingBean
    @Bean
    public RebootController rebootController() {
        return new RebootController();
    }

    @Bean
    @ConditionalOnBean(GrpcClientProxy.class)
    public GrpcClientShutdownConfig grpcClientShutdownConfig() {
        return new GrpcClientShutdownConfig();
    }

    @Bean
    @ConditionalOnBean(GrpcServiceProxy.class)
    public GrpcServerShutdownConfig grpcServerShutdownConfig() {
        return new GrpcServerShutdownConfig();
    }
}
