package com.jindi.infra.leaf.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "leaf")
public class LeafConfigProperties {

	private String segment = "http://10.2.16.70:12780/api/segment/get/";

	private String snowflake = "http://10.2.16.70:12780/api/snowflake/get/";

	private Integer successCode = 200;

	private Integer maxTryTime = 3;

	private Integer connectTimeout = 3000;

	private Integer timeout = 500;
}
