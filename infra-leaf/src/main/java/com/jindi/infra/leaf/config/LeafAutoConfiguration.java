package com.jindi.infra.leaf.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.jindi.infra.leaf.IdGenerator;
import com.jindi.infra.leaf.properties.LeafConfigProperties;
import com.jindi.infra.tools.retry.RetryConfiguration;

@Configuration
@EnableConfigurationProperties(LeafConfigProperties.class)
@Import(RetryConfiguration.class)
public class LeafAutoConfiguration {

	@ConditionalOnMissingBean(name = "idGenerator")
	@Bean(name = "idGenerator")
	public IdGenerator idGenerator(LeafConfigProperties leafConfigProperties) {
		return new IdGenerator(leafConfigProperties);
	}

	@ConditionalOnMissingBean(name = "leafRetryTemplate")
	@Bean(name = "leafRetryTemplate")
	public RetryTemplate leafRetryTemplate(LeafConfigProperties leafConfigProperties) {
		RetryTemplate retryTemplate = new RetryTemplate();
		SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
		simpleRetryPolicy.setMaxAttempts(3);
		retryTemplate.setRetryPolicy(simpleRetryPolicy);
		FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(10);
		retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
		retryTemplate.setThrowLastExceptionOnExhausted(true);
		return retryTemplate;
	}
}
