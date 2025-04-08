package com.jindi.infra.grpc.client;

import java.util.List;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.util.ACUtils;

/**
 * 自定义的请求开始和结束的拦截器
 */
public class RefreshedCallInterceptorsReference implements ApplicationListener<ContextRefreshedEvent> {

	private SimpleClientInterceptor simpleClientInterceptor;

	public RefreshedCallInterceptorsReference(SimpleClientInterceptor simpleClientInterceptor) {
		this.simpleClientInterceptor = simpleClientInterceptor;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
		if (simpleClientInterceptor != null) {
			List<CallInterceptor> callInterceptors = ACUtils.getBeansOfType(CallInterceptor.class);
			if (!CollectionUtils.isEmpty(callInterceptors)) {
				AnnotationAwareOrderComparator.sort(callInterceptors);
			}
			simpleClientInterceptor.setCallInterceptorsReference(callInterceptors);
		}
	}
}
