package com.jindi.infra.topology.grpc.config;

import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.topology.grpc.filter.TopologyGRPCCallInterceptor;
import com.jindi.infra.topology.grpc.filter.TopologyGRPCCoreServerInterceptor;
import com.jindi.infra.topology.grpc.filter.TopologyGRPCServerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass({CallInterceptor.class, RequestFilter.class})
@Configuration
public class TopologyGrpcAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public TopologyGRPCCallInterceptor topologyGRPCCallInterceptor() {
        return new TopologyGRPCCallInterceptor();
    }

    @ConditionalOnMissingBean(name = "topologyGRPCCoreServerInterceptor")
    @Bean(name = "topologyGRPCCoreServerInterceptor")
    public TopologyGRPCCoreServerInterceptor topologyGRPCCoreServerInterceptor() {
        return new TopologyGRPCCoreServerInterceptor();
    }

    @ConditionalOnMissingBean
    @Bean
    public TopologyGRPCServerInterceptor topologyGRPCServerInterceptor() {
        return new TopologyGRPCServerInterceptor();
    }


}
