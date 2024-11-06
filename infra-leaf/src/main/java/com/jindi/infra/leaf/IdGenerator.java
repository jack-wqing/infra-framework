package com.jindi.infra.leaf;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import com.jindi.infra.common.util.InnerRestClientUtils;
import com.jindi.infra.leaf.properties.LeafConfigProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdGenerator {

	private final LeafConfigProperties leafConfigProperties;

	@Resource(name = "leafRetryTemplate")
	private RetryTemplate leafRetryTemplate;

	public IdGenerator(LeafConfigProperties leafConfigProperties) {
		this.leafConfigProperties = leafConfigProperties;
	}

	public Long getSegmentId(String key) {
		if (StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("key require non empty");
		}
		try {
			long id;
			id = leafRetryTemplate.execute((RetryCallback<Long, Throwable>) retryContext -> {
				String data = InnerRestClientUtils.getForObject(leafConfigProperties.getSegment() + key, String.class,
						leafConfigProperties.getConnectTimeout(), leafConfigProperties.getTimeout());
				return Long.parseLong(data);
			});
			return id;
		} catch (Throwable e) {
			log.error("", e);
		}
		return -1L;
	}

	public Long getSnowflakeId(String key) {
		if (StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("key require non empty");
		}
		try {
			long id;
			id = leafRetryTemplate.execute((RetryCallback<Long, Throwable>) retryContext -> {
				String data = InnerRestClientUtils.getForObject(leafConfigProperties.getSnowflake() + key, String.class,
						leafConfigProperties.getConnectTimeout(), leafConfigProperties.getTimeout());
				return Long.parseLong(data);
			});
			return id;
		} catch (Throwable e) {
			log.error("", e);
		}
		return -1L;
	}
}
