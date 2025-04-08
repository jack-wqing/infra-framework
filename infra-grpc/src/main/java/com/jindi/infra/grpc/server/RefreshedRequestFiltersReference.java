package com.jindi.infra.grpc.server;

import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.grpc.util.ACUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import java.util.List;

// 系统自定义的拦截器: 通过实现 RequestFilter
public class RefreshedRequestFiltersReference implements ApplicationListener<ContextRefreshedEvent> {

	private SimpleServerInterceptor simpleServerInterceptor;

	public RefreshedRequestFiltersReference(SimpleServerInterceptor simpleServerInterceptor) {
		this.simpleServerInterceptor = simpleServerInterceptor;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
		if (simpleServerInterceptor != null) {
			List<RequestFilter> requestFilters = ACUtils.getBeansOfType(RequestFilter.class);
			if (!CollectionUtils.isEmpty(requestFilters)) {
				AnnotationAwareOrderComparator.sort(requestFilters);
			}
			simpleServerInterceptor.setRequestFiltersReference(requestFilters);
		}
	}

}
