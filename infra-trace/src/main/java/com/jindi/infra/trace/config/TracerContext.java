package com.jindi.infra.trace.config;

import org.springframework.beans.factory.InitializingBean;

import com.jindi.infra.trace.constant.TracePropagation;

import io.opentracing.Tracer;

public class TracerContext implements InitializingBean {

	private Tracer tracer;

	public TracerContext(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void afterPropertiesSet() {
		TracePropagation.tracer = tracer;
	}
}
