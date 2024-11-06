package com.jindi.infra.trace.oneservice;

import com.jindi.infra.dataapi.oneservice.annotation.OneService;
import com.jindi.infra.dataapi.oneservice.annotation.OneServiceApi;
import com.jindi.infra.dataapi.oneservice.client.OneServiceHttpClient;
import com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts;
import com.jindi.infra.dataapi.oneservice.param.OneServiceParam;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.jindi.infra.trace.utils.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

@Slf4j
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class OneServiceTracingAspect {

	@Resource
	private OneServiceHttpClient oneServiceHttpClient;
	@Resource
	private TraceContext traceContext;

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
		Span span = null;
		try {
			OneServiceParam oneServiceParam = OneServiceConsts.ONE_SERVICE_PARAM_THREAD_LOCAL.get();
			String path = oneServiceHttpClient.getInvokePath(oneServiceParam);
			String urlPrefix = oneServiceHttpClient.getUrlPrefix(path);
			TracePropagation currentTracePropagation = TraceMDCUtil.getCurrentTracePropagation();
			TracePropagation trace = traceContext.createCSTracePropagation(currentTracePropagation);
			span = createSpan(trace, method, urlPrefix, path);
			TraceUtil.tagCatMessageId(span);
			return pjp.proceed();
		} catch (Throwable e) {
			TraceUtil.tag(span, TagsConsts.ERROR, e.getMessage());
			throw e;
		} finally {
			traceContext.writeSpan(span);
		}
	}

	private Span createSpan(TracePropagation trace, String method, String urlPrefix, String apiPath) {
		Span span = traceContext.buildSpan(trace, method, Span.KindEnum.CLIENT, "oneservice");
		TraceUtil.tag(span, "appname", urlPrefix);
		TraceUtil.tag(span, "http.path", apiPath);
		return span;
	}
}
