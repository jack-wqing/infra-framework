package com.jindi.infra.grpc.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.grpc.util.ACUtils;

/**
 * 刷新RequestFilter列表信息
 */
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
