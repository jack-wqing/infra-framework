package com.jindi.infra.tools.retry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 重试组件
 *
 * <p>
 * 文档 https://github.com/spring-projects/spring-retry
 *
 * @see com.jindi.infra.tools.retry.Sample
 */
@Configuration
@EnableRetry
public class RetryConfiguration {

	/**
	 * 默认重试模版
	 *
	 * @return
	 */
	@ConditionalOnMissingBean(name = "defaultRetryTemplate")
	@Bean(name = "defaultRetryTemplate")
	public RetryTemplate defaultRetryTemplate() {
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
