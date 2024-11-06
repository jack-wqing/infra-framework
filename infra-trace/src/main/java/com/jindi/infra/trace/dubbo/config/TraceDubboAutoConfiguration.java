package com.jindi.infra.trace.dubbo.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.trace.utils.SpringBeanUtils;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration;

@Configuration
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TracerAutoConfiguration.class)
@ConditionalOnClass(com.alibaba.dubbo.rpc.Invocation.class)
@ConditionalOnProperty(value = "trace.dubbo.enable", matchIfMissing = true)
public class TraceDubboAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public SpringBeanUtils springBeanUtils() {
		return new SpringBeanUtils();
	}
}
