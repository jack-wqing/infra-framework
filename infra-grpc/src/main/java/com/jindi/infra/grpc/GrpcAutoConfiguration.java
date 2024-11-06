package com.jindi.infra.grpc;

import com.dianping.cat.message.Transaction;
import com.jindi.infra.grpc.server.*;
import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import com.jindi.infra.core.aspect.MultiProtocolServerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.jindi.infra.common.constant.RegionConstant;
import com.jindi.infra.grpc.channel.ChannelManager;
import com.jindi.infra.grpc.client.*;
import com.jindi.infra.grpc.lifecycle.GrpcServiceAutoRegistration;
import com.jindi.infra.grpc.provider.GrpcProvider;
import com.jindi.infra.grpc.util.ACUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

	@ConditionalOnClass(Transaction.class)
	@Bean
	public CatGrpcClientHandler catGrpcClientHandler() {
		return new CatGrpcClientHandler();
	}

	@ConditionalOnMissingBean
	@Bean
	public GrpcInfraContextClientInterceptor grpcInfraContextClientInterceptor() {
		return new GrpcInfraContextClientInterceptor();
	}

	@ConditionalOnMissingBean
	@Bean
	public GrpcInfraContextServerInterceptor grpcInfraContextServerInterceptor() {
		return new GrpcInfraContextServerInterceptor();
	}

	@ConditionalOnBean(type = {"com.jindi.infra.core.aspect.MultiProtocolClientInterceptor"})
	@ConditionalOnMissingBean
	@Bean
	public GrpcClientMultiProtocolInterceptor grpcClientMultiProtocolInterceptor(List<MultiProtocolClientInterceptor> interceptorList) {
		return new GrpcClientMultiProtocolInterceptor(interceptorList);
	}

	@ConditionalOnBean(type = {"com.jindi.infra.core.aspect.MultiProtocolServerInterceptor"})
	@ConditionalOnMissingBean
	@Bean
	public GrpcServerMultiProtocolInterceptor grpcServerMultiProtocolInterceptor(List<MultiProtocolServerInterceptor> interceptorList) {
		return new GrpcServerMultiProtocolInterceptor(interceptorList);
	}
}
