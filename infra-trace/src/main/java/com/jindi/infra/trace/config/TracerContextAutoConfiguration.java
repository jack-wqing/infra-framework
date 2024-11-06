package com.jindi.infra.trace.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration;

@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TracerAutoConfiguration.class)
@ConditionalOnProperty(value = "trace.context.enable", matchIfMissing = true)
public class TracerContextAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public TracerContext traceContext(Tracer tracer) {
		return new TracerContext(tracer);
	}
}
