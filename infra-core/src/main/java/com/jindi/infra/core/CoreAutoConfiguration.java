package com.jindi.infra.core;

import com.jindi.infra.core.aspect.RefreshedCoreRpcServerInterceptorsReference;
import com.jindi.infra.core.util.ACUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public ACUtils coreACUtils() {
		return new ACUtils();
	}

	@ConditionalOnMissingBean
	@Bean
	public RefreshedCoreRpcServerInterceptorsReference refreshedCoreRpcServerInterceptorsReference() {
		return new RefreshedCoreRpcServerInterceptorsReference();
	}
}
