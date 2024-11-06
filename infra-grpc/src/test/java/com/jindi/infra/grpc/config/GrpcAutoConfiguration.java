package com.jindi.infra.grpc.config;

import com.jindi.infra.common.constant.RegionConstant;
import com.jindi.infra.grpc.channel.ChannelManager;
import com.jindi.infra.grpc.server.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.jindi.infra.grpc.RpcProperties;
import com.jindi.infra.grpc.client.*;
import com.jindi.infra.grpc.lifecycle.GrpcServiceAutoRegistration;
import com.jindi.infra.grpc.provider.GrpcProvider;
import com.jindi.infra.grpc.util.ACUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * grpc自动配置类; 默认不需要手动导入
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "rpc.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RpcProperties.class)
public class GrpcAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public GrpcServiceProxy grpcServiceProxy() {
		return new GrpcServiceProxy();
	}

	@Bean
	@ConditionalOnMissingBean
	public RPCServiceBeanPostProcessor rpcServiceBeanPostProcessor() {
		return new RPCServiceBeanPostProcessor();
	}

	@Bean
	@ConditionalOnMissingBean
	public GrpcServiceAutoRegistration grpcServiceAutoRegistration(GrpcServiceProxy grpcServiceProxy,
			GrpcClientProxy grpcClientProxy) {
		return new GrpcServiceAutoRegistration(grpcServiceProxy, grpcClientProxy);
	}

	@ConditionalOnMissingBean(name = "aliyunChannelManager")
	@Bean(name = "aliyunChannelManager")
	public ChannelManager aliyunChannelManager() {
		return new ChannelManager(RegionConstant.ALIYUN_REGION);
	}

	@ConditionalOnMissingBean(name = "huaweiChannelManager")
	@Bean(name = "huaweiChannelManager")
	public ChannelManager huaweiChannelManager() {
		return new ChannelManager(RegionConstant.HUAWEI_REGION);
	}

	@ConditionalOnMissingBean
	@Bean
	public CallContextManager callContextManager() {
		return new CallContextManager();
	}

	@ConditionalOnMissingBean
	@Bean
	public GrpcClientProxy grpcClientProxy() {
		return new GrpcClientProxy();
	}

	@ConditionalOnMissingBean
	@Bean
	public RPCCallBeanPostProcessor rpcCallBeanPostProcessor() {
		return new RPCCallBeanPostProcessor();
	}

	@ConditionalOnMissingBean
	@Bean
	public ACUtils acUtils() {
		return new ACUtils();
	}

	@ConditionalOnMissingBean
	@Bean
	public SimpleClientInterceptor simpleClientInterceptor() {
		return new SimpleClientInterceptor();
	}

	@ConditionalOnMissingBean
	@Bean
	public RefreshedCallInterceptorsReference refreshedCallInterceptorsReference(
			SimpleClientInterceptor simpleClientInterceptor) {
		return new RefreshedCallInterceptorsReference(simpleClientInterceptor);
	}

	@ConditionalOnMissingBean
	@Bean
	public SimpleServerInterceptor simpleServerInterceptor() {
		return new SimpleServerInterceptor();
	}

	@ConditionalOnMissingBean
	@Bean
	public RefreshedRequestFiltersReference refreshedRequestFiltersReference(
			SimpleServerInterceptor simpleServerInterceptor) {
		return new RefreshedRequestFiltersReference(simpleServerInterceptor);
	}

	@ConditionalOnMissingBean
	@Bean
	public HeaderContextServerInterceptor headerContextServerInterceptor () {
		return new HeaderContextServerInterceptor();
	}

	@ConditionalOnWebApplication
	@ConditionalOnMissingBean
	@Bean
	public GrpcProvider grpcProvider() {
		return new GrpcProvider();
	}
}
