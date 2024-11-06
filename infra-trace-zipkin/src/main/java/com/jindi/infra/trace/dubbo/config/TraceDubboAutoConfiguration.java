package com.jindi.infra.trace.dubbo.config;

import com.alibaba.dubbo.rpc.Invocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.trace.dubbo.context.DubboTraceContext;

@Configuration
@ConditionalOnClass(Invocation.class)
public class TraceDubboAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DubboTraceContext dubboTraceContext() {
        return new DubboTraceContext();
    }

}
