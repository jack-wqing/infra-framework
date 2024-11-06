package com.jindi.infra.tools.retry;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * 重试组件示例
 */
@Component
public class Sample {

	@Resource(name = "defaultRetryTemplate")
	private RetryTemplate defaultRetryTemplate;

	/**
	 * 捕获IllegalArgumentException异常进行重试，最大重试次数3，每次重试延迟10*（n-1)*2毫秒执行,n为重试次数
	 *
	 * @return
	 */
	@Retryable(include = IllegalArgumentException.class, maxAttempts = 3, backoff = @Backoff(delay = 10, multiplier = 2))
	public Integer randomInt() {
		int n = RandomUtils.nextInt(0, 2);
		if (n == 0) {
			throw new IllegalArgumentException();
		}
		return n;
	}

	public Integer outerRandomInt() {
		return defaultRetryTemplate.execute(new RetryCallback<Integer, IllegalArgumentException>() {
			@Override
			public Integer doWithRetry(RetryContext retryContext) throws IllegalArgumentException {
				int n = RandomUtils.nextInt(0, 2);
				if (n == 0) {
					throw new IllegalArgumentException();
				}
				return n;
			}
		});
	}
}
