package com.jindi.infra.registry.nacos.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "nacos.discovery")
@Data
public class NacosProperties {
	/**
	 * server地址
	 */
	private String serverAddr = "localhost:9999";
	/**
	 * 是否自动注册，默认开启
	 */
	private boolean autoRegister = true;
	/**
	 * 用户名
	 */
	private String username = "nacos";
	/**
	 * 密码
	 */
	private String password = "nacos";
	/**
	 * 全局path
	 */
	private String contextPath;
	/**
	 * 注册集群名
	 */
	private String clusterName;
	/**
	 *
	 */
	private String endpoint;
	/**
	 * 多租户命名空间
	 */
	private String namespace;
	/**
	 * 密钥
	 */
	private String accessKey;

	private String secretKey;

	/**
	 * 是否开启推空保护
	 */
	private String namingPushEmptyProtection = "true";
}
