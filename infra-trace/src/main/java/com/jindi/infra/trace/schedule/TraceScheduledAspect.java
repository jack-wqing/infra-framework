/**
 * Copyright 2017-2019 The OpenTracing Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jindi.infra.trace.schedule;

import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.cloud.ExtensionTags;
import io.opentracing.contrib.spring.cloud.SpanUtils;
import io.opentracing.contrib.spring.cloud.scheduled.ScheduledTracingProperties;
import io.opentracing.tag.Tags;

/**
 * 这里复用jaeger
 * ScheduleAspect的代码，原因是@Aspect没有显示执行Order，不能在其之后做日志中显示traceId的扩展，因此把代码原样复制一份
 */
@Aspect
public class TraceScheduledAspect {

	static final String COMPONENT_NAME = "scheduled";

	private Tracer tracer;
	private Pattern skipPattern;

	public TraceScheduledAspect(Tracer tracer, ScheduledTracingProperties scheduledTracingProperties) {
		this.tracer = tracer;
		this.skipPattern = Pattern.compile(scheduledTracingProperties.getSkipPattern());
	}

	@Around("execution (@org.springframework.scheduling.annotation.Scheduled  * *.*(..))")
	public Object traceAspect(final ProceedingJoinPoint pjp) throws Throwable {
		if (skipPattern.matcher(pjp.getTarget().getClass().getName()).matches()) {
			return pjp.proceed();
		}

		Span span = tracer.buildSpan(pjp.getSignature().getName()).withTag(Tags.COMPONENT.getKey(), COMPONENT_NAME)
				.withTag(ExtensionTags.CLASS_TAG.getKey(), pjp.getTarget().getClass().getSimpleName())
				.withTag(ExtensionTags.METHOD_TAG.getKey(), pjp.getSignature().getName()).start();
		try {
			try (Scope scope = tracer.activateSpan(span)) {
				// 补充这一句
				TraceMDCUtil.putTraceInfo(span.context());
				return pjp.proceed();
			}
		} catch (Exception ex) {
			SpanUtils.captureException(span, ex);
			throw ex;
		} finally {
			span.finish();
			TraceMDCUtil.clear();
		}
	}
}
