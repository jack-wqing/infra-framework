package com.jindi.infra.job.trace;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.jindi.infra.job.util.JobMDCUtil;

@Aspect
public class JobMDCAspect {

    @Around("execution (@com.xxl.job.core.handler.annotation.XxlJob  * *.*(..))")
    public Object jobTraceAspect(final ProceedingJoinPoint pjp) throws Throwable {
        try {
            JobMDCUtil.createTrace();
            return pjp.proceed();
        } finally {
            JobMDCUtil.cleanTrace();
        }
    }

}
