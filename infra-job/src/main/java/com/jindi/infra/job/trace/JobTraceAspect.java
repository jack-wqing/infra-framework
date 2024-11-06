package com.jindi.infra.job.trace;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class JobTraceAspect {

	@Resource
	private TraceContext traceContext;

    @Around("execution (@com.xxl.job.core.handler.annotation.XxlJob  * *.*(..))")
    public Object jobTraceAspect(final ProceedingJoinPoint pjp) throws Throwable {
        Span span = createSpan(pjp);
        try {
            return pjp.proceed();
        } finally {
            traceContext.writeSpan(span);
            TraceMDCUtil.clean();
        }
    }

    private Span createSpan(ProceedingJoinPoint pjp) {
        try {
            TracePropagation tracePropagation = traceContext.createTracePropagation();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            XxlJob xxlJob = signature.getMethod().getAnnotation(XxlJob.class);
            return traceContext.buildSpan(tracePropagation, xxlJob.value(), Span.KindEnum.SERVER, "xxl-job");
        } catch (Exception e) {
            log.error("xxljob生成span error", e);
        }
        return null;
    }

}
