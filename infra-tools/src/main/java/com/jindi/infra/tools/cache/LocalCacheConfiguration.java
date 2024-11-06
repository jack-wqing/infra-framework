package com.jindi.infra.tools.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalCacheConfiguration {

	@ConditionalOnMissingBean(name = "defaultLocalCache")
	@Bean(name = "defaultLocalCache")
	public LocalCache<Object, Object> defaultLocalCache() {
		return LocalCache.create(5000, 30000);
	}
}
