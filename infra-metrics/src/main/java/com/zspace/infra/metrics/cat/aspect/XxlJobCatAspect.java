package com.zspace.infra.metrics.cat.aspect;


import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
public class XxlJobCatAspect {

    private static final String CAT_TYPE = "XXL-JOB";

    @Around("execution (@com.xxl.job.core.handler.annotation.XxlJob  * *.*(..))")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        XxlJob xxlJob = signature.getMethod().getAnnotation(XxlJob.class);
        Transaction transaction = Cat.newTransaction(CAT_TYPE, xxlJob.value());
        Object result = null;
        try {
            result = pjp.proceed();
            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable e) {
            transaction.setStatus(e);
            throw e;
        } finally {
            transaction.complete();
        }
        return result;
    }

}
