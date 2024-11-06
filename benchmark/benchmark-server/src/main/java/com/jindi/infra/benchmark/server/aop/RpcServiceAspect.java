package com.jindi.infra.benchmark.server.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Configuration
public class RpcServiceAspect {

	@Pointcut("execution(public * com.jindi.infra.benchmark.server.rpc..*.*(..))")
	public void pointcut() {
	}

	@Around("pointcut()")
	public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		log.info("牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼，牛逼");
		return proceedingJoinPoint.proceed();
	}
}
