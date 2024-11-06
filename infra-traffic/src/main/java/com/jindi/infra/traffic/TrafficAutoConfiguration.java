package com.jindi.infra.traffic;

import com.jindi.infra.traffic.sentinel.mvc.parser.SentinelHeaderOriginParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.grpc.client.GrpcClientProxy;
import com.jindi.infra.grpc.server.GrpcServiceProxy;
import com.jindi.infra.traffic.sentinel.filter.HeaderRequestFilter;
import com.jindi.infra.traffic.sentinel.interceptor.*;

import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;

@Configuration
@ConditionalOnProperty(name = {"rpc.enable", "rpc.traffic.enable"}, havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({ClientInterceptor.class, ServerInterceptor.class})
public class TrafficAutoConfiguration {

	/**
	 * sentinel实现服务端限流
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(GrpcServiceProxy.class)
	public SentinelGrpcServerInterceptor sentinelGrpcServerInterceptor() {
		return new SentinelGrpcServerInterceptor();
	}

	/**
	 * sentinel实现客户端限流
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(GrpcClientProxy.class)
	public SentinelGrpcClientInterceptor sentinelGrpcClientInterceptor() {
		return new SentinelGrpcClientInterceptor();
	}

	/**
	 * server端header传递
	 */
	@Bean
	@ConditionalOnMissingBean
	public HeaderRequestFilter headerRequestFilter() {
		return new HeaderRequestFilter();
	}

	/**
	 * client端header传递
	 */
	@Bean
	@ConditionalOnMissingBean
	public HeaderCallInterceptor headerCallInterceptor(@Value("${spring.application.name}") String applicationName) {
		return new HeaderCallInterceptor(applicationName);
	}

	/**
	 * client端设设置上下文
	 */
	@Bean
	@ConditionalOnMissingBean
	public ContextEnterClientInterceptor contextEnterClientInterceptor(
			@Value("${spring.application.name}") String applicationName) {
		return new ContextEnterClientInterceptor(applicationName);
	}

	/**
	 * client端清除上下文
	 */
	@Bean
	@ConditionalOnMissingBean
	public ContextClearClientInterceptor contextClearClientInterceptor() {
		return new ContextClearClientInterceptor();
	}

	/**
	 * server端解析Origin
	 */
	@ConditionalOnMissingBean(name = "sentinelHeaderOriginParser")
	@Bean(name = "sentinelHeaderOriginParser")
	public SentinelHeaderOriginParser sentinelHeaderOriginParser() {return new SentinelHeaderOriginParser();}
}
