package com.jindi.infra.metrics.cat.config;

import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.metrics.cat.aspect.XxlJobCatAspect;
import com.jindi.infra.metrics.cat.interceptor.CatGrpcCoreServerInterceptor;
import com.jindi.infra.metrics.cat.interceptor.latency.GrpcLatencyClientInterceptor;
import com.jindi.infra.metrics.cat.interceptor.latency.GrpcLatencyCoreServerInterceptor;
import com.jindi.infra.metrics.cat.interceptor.latency.GrpcLatencyServerInterceptor;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.dianping.cat.servlet.CatFilter;
import com.jindi.infra.metrics.cat.interceptor.CatGrpcCrossClientInterceptor;

import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;

/**
 * @author changbo
 * @date 2021/7/14
 */
@Configuration
@ConditionalOnProperty(name = "cat.enable", havingValue = "true", matchIfMissing = true)
@Import({CatAutoConfiguration.CatGrpcAutoConfiguration.class})
public class CatAutoConfiguration {

	@ConditionalOnWebApplication
	@ConditionalOnMissingBean(name = "catFilter")
	@Bean
	public FilterRegistrationBean<CatFilter> catFilter() {
		FilterRegistrationBean<CatFilter> registration = new FilterRegistrationBean<>();
		CatFilter filter = new CatFilter();
		registration.setFilter(filter);
		registration.addUrlPatterns("/*");
		registration.setName("cat-filter");
		registration.setOrder(1);
		return registration;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = {"com.xxl.job.core.handler.IJobHandler"})
	public XxlJobCatAspect xxlJobCatAspect() {
		return new XxlJobCatAspect();
	}

	@Configuration
	@ConditionalOnProperty(name = "rpc.enable", havingValue = "true", matchIfMissing = true)
	@ConditionalOnClass({ServerInterceptor.class, ClientInterceptor.class, CoreRpcServerInterceptor.class})
	public static class CatGrpcAutoConfiguration {

		@ConditionalOnMissingBean(name = "catGrpcCrossClientInterceptor")
		@Bean(name = "catGrpcCrossClientInterceptor")
		public CatGrpcCrossClientInterceptor catGrpcCrossClientInterceptor() {
			return new CatGrpcCrossClientInterceptor();
		}

		@ConditionalOnMissingBean(name = "catGrpcCoreServerInterceptor")
		@Bean(name = "catGrpcCoreServerInterceptor")
		public CatGrpcCoreServerInterceptor catGrpcCoreServerInterceptor() {
			return new CatGrpcCoreServerInterceptor();
		}

		@ConditionalOnMissingBean(name = "grpcLatencyServerInterceptor")
		@Bean(name = "grpcLatencyServerInterceptor")
		public GrpcLatencyServerInterceptor grpcLatencyServerInterceptor() {
			return new GrpcLatencyServerInterceptor();
		}

		@ConditionalOnMissingBean(name = "grpcLatencyClientInterceptor")
		@Bean(name = "grpcLatencyClientInterceptor")
		public GrpcLatencyClientInterceptor grpcLatencyClientInterceptor() {
			return new GrpcLatencyClientInterceptor();
		}

		@ConditionalOnMissingBean(name = "grpcLatencyCoreServerInterceptor")
		@Bean(name = "grpcLatencyCoreServerInterceptor")
		public GrpcLatencyCoreServerInterceptor grpcLatencyCoreServerInterceptor() {
			return new GrpcLatencyCoreServerInterceptor();
		}
	}
}
