package com.jindi.infra.job.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "job")
public class JobProperties {

	private String logPath;

	private String url;

	private Integer logRetentionDays;

	private Integer jobPort;
}
