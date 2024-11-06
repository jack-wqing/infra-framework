package com.jindi.infra.dataapi.oneservice.aspect;

import com.jindi.infra.dataapi.oneservice.annotation.OneService;
import com.jindi.infra.dataapi.oneservice.annotation.OneServiceApi;
import com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts;
import com.jindi.infra.dataapi.oneservice.param.OneServiceParam;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.dataapi.spacecloud.constant.SpaceCloudConsts;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class OneServiceAspect {

	@Around("@annotation(api)")
	public Object doAround(ProceedingJoinPoint pjp, OneServiceApi api) throws Throwable {
		Signature sig = pjp.getSignature();
		if (!(sig instanceof MethodSignature)) {
			return pjp.proceed();
		}
		MethodSignature methodSignature = (MethodSignature) sig;
		Class<?> declaringClass = methodSignature.getMethod().getDeclaringClass();
		OneService oneService = declaringClass.getAnnotation(OneService.class);
		if (oneService == null) {
			return pjp.proceed();
		}
		String method = String.format("%s.%s", declaringClass.getSimpleName(), methodSignature.getMethod().getName());
		Transaction transaction = null;
		OneServiceParam oneServiceParam = null;
		try {
			oneServiceParam = createOneServiceParam(oneService, api);
			OneServiceConsts.ONE_SERVICE_PARAM_THREAD_LOCAL.set(oneServiceParam);
			transaction = Cat.newTransaction(OneServiceConsts.ONE_SERVICE, method);
			Object returnValue = pjp.proceed();
			transaction.setStatus(Message.SUCCESS);
			return returnValue;
		} catch (Throwable e) {
			Cat.logError(e);
			if (transaction != null) {
				transaction.setStatus("ERROR");
			}
			log.error("spaceCloudParam = {}, args = {} 失败", InnerJSONUtils.toJSONString(oneServiceParam),
					InnerJSONUtils.toJSONString(pjp.getArgs()), e);
			throw e;
		} finally {
			SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.remove();
			if (transaction != null) {
				transaction.complete();
			}
		}
	}

	private OneServiceParam createOneServiceParam(OneService oneService, OneServiceApi api) {
		return new OneServiceParam(oneService.project(), oneService.folder(), api.api(), api.version());
	}
}
