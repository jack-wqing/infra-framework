package com.jindi.infra.trace.grpc.config;

import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.extension.RequestFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.trace.grpc.context.GrpcTraceContext;
import com.jindi.infra.trace.grpc.filter.TraceCallInterceptor;
import com.jindi.infra.trace.grpc.filter.TraceRequestFilter;
import com.jindi.infra.trace.model.TraceContext;

@ConditionalOnClass({CallInterceptor.class, RequestFilter.class})
@Configuration
public class TraceGrpcAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public TraceContext traceContext() {
        return new TraceContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcTraceContext grpcTraceContext() {
        return new GrpcTraceContext();
    }

    @ConditionalOnMissingBean
    @Bean
    public TraceCallInterceptor traceCallInterceptor (TraceContext traceContext) {
        return new TraceCallInterceptor(traceContext);
    }

    @ConditionalOnMissingBean
    @Bean
    public TraceRequestFilter traceRequestFilter (TraceContext traceContext) {
        return new TraceRequestFilter(traceContext);
    }
}
