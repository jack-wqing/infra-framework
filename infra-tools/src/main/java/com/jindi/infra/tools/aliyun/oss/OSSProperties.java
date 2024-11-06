package com.jindi.infra.tools.aliyun.oss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("aliyun.oss")
public class OSSProperties {

	private String endpoint;
	private String accessKeyId;
	private String accessKeySecret;
	private Integer connectionRequestTimeout = 100;
	private Integer socketTimeout = 100;
	private Integer requestTimeout = 200;
}
