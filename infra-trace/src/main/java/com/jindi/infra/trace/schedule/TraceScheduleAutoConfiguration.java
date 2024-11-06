package com.jindi.infra.trace.schedule;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.scheduled.ScheduledTracingProperties;
import io.opentracing.contrib.spring.tracer.configuration.TracerAutoConfiguration;

/**
 * @author changbo <changbo@kuaishou.com> Created on 2020-03-11
 */
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TracerAutoConfiguration.class)
@EnableConfigurationProperties(ScheduledTracingProperties.class)
@ConditionalOnProperty(value = "trace.schedule.enable", matchIfMissing = true)
public class TraceScheduleAutoConfiguration {

	@Bean
	public TraceScheduledAspect traceScheduledAspect(Tracer tracer,
			ScheduledTracingProperties scheduledTracingProperties) {
		return new TraceScheduledAspect(tracer, scheduledTracingProperties);
	}
}
