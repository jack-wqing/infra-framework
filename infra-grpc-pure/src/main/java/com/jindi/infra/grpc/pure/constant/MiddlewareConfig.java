package com.jindi.infra.grpc.pure.constant;

import java.util.Objects;

public enum MiddlewareConfig {
	ALIYUN_DEV(CloudPlatformEnum.ALIYUN, EnvEnum.DEV, "nacos-dev.middleware.huawei:8848",
			"sentinel-dev.services.huawei:80", "nacos", "nacos", "sentinel", ""),
	ALIYUN_TEST(CloudPlatformEnum.ALIYUN, EnvEnum.TEST, "nacos-test.middleware.huawei:8848",
			"sentinel-test.services.huawei:80", "nacos", "nacos", "sentinel", ""),
	ALIYUN_YUFA(CloudPlatformEnum.ALIYUN, EnvEnum.YUFA, "nacos-pre.middleware.huawei:8848",
			"sentinel-pre.services.huawei:80", "nacos", "nacos", "sentinel", ""),
	ALIYUN_PROD(CloudPlatformEnum.ALIYUN, EnvEnum.PROD, "172.24.116.157:8848",
			"sentinel.services.huawei:80", "nacos", "nacos", "sentinel", ""),
	HUAWEI_DEV(CloudPlatformEnum.HUAWEI, EnvEnum.DEV, "nacos-dev.middleware.huawei:8848",
			"sentinel-dev.services.huawei:80", "nacos", "nacos", "sentinel", "pushgateway.middleware.huawei:9091"),
	HUAWEI_TEST(CloudPlatformEnum.HUAWEI, EnvEnum.TEST, "nacos-test.middleware.huawei:8848",
			"sentinel-test.services.huawei:80", "nacos", "nacos", "sentinel", "pushgateway.middleware.huawei:9091"),
	HUAWEI_YUFA(CloudPlatformEnum.HUAWEI, EnvEnum.YUFA, "nacos-pre.middleware.huawei:8848",
			"sentinel-pre.services.huawei:80", "nacos", "nacos", "sentinel", "pushgateway.middleware.huawei:9091"),
	HUAWEI_PROD(CloudPlatformEnum.HUAWEI, EnvEnum.PROD, "nacos.middleware.huawei:8848",
			"sentinel.services.huawei:80", "nacos", "nacos", "sentinel", "pushgateway.middleware.huawei:9091");
	private final CloudPlatformEnum cloudPlatform;
	private final EnvEnum env;
	private final String nacosServerAddress;
	private final String sentinelDashboardServer;
	private final String nacosUsername;
	private final String nacosPassword;
	private final String sentinelNamespace;
	private final String prometheusGatewayAddress;

	MiddlewareConfig(CloudPlatformEnum cloudPlatform, EnvEnum env, String nacosServerAddress,
			String sentinelDashboardServer, String nacosUsername, String nacosPassword, String sentinelNamespace,
			String prometheusGatewayAddress) {
		this.cloudPlatform = cloudPlatform;
		this.env = env;
		this.nacosServerAddress = nacosServerAddress;
		this.sentinelDashboardServer = sentinelDashboardServer;
		this.nacosUsername = nacosUsername;
		this.nacosPassword = nacosPassword;
		this.sentinelNamespace = sentinelNamespace;
		this.prometheusGatewayAddress = prometheusGatewayAddress;
	}

	public static MiddlewareConfig getMiddlewareConfigByCloudPlatformAndEnv(CloudPlatformEnum cloudPlatform,
			EnvEnum env) {
		for (MiddlewareConfig middlewareConfig : values()) {
			if (Objects.equals(cloudPlatform, middlewareConfig.getCloudPlatform())
					&& Objects.equals(env, middlewareConfig.getEnv())) {
				return middlewareConfig;
			}
		}
		return null;
	}

	public CloudPlatformEnum getCloudPlatform() {
		return cloudPlatform;
	}

	public EnvEnum getEnv() {
		return env;
	}

	public String getNacosServerAddress() {
		return nacosServerAddress;
	}

	public String getSentinelDashboardServer() {
		return sentinelDashboardServer;
	}

	public String getNacosUsername() {
		return nacosUsername;
	}

	public String getNacosPassword() {
		return nacosPassword;
	}

	public String getSentinelNamespace() {
		return sentinelNamespace;
	}

	public String getPrometheusGatewayAddress() {
		return prometheusGatewayAddress;
	}
}
