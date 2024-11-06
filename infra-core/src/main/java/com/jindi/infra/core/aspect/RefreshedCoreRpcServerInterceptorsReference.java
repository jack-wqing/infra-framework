package com.jindi.infra.core.aspect;

import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.core.util.CoreRpcServerAspect;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 刷新RequestInterceptor列表信息
 */
public class RefreshedCoreRpcServerInterceptorsReference implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
		List<CoreRpcServerInterceptor> callInterceptors = ACUtils.getBeansOfType(CoreRpcServerInterceptor.class);
		if (!CollectionUtils.isEmpty(callInterceptors)) {
			CoreRpcServerAspect.init(callInterceptors);
		}
	}
}