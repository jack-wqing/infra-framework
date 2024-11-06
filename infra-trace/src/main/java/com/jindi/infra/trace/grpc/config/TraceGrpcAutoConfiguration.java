package com.jindi.infra.trace.grpc.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.jindi.infra.trace.grpc.interceptor.TraceGrpcCallInterceptor;
import com.jindi.infra.trace.grpc.interceptor.TraceGrpcRequestFilter;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.scheduled.ScheduledTracingProperties;
import io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration;

@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TracerAutoConfiguration.class)
@EnableConfigurationProperties(ScheduledTracingProperties.class)
@ConditionalOnProperty(value = "trace.grpc.enable", matchIfMissing = true)
public class TraceGrpcAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public TraceGrpcCallInterceptor traceCallInterceptor(Tracer tracer) {
		return new TraceGrpcCallInterceptor(tracer);
	}

	@ConditionalOnMissingBean
	@Bean
	public TraceGrpcRequestFilter traceRequestFilter(Tracer tracer) {
		return new TraceGrpcRequestFilter(tracer);
	}
}
