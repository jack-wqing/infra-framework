package com.jindi.infra.grpc.pure.constant;

public enum CloudPlatformEnum {
	ALIYUN("aliyun"), HUAWEI("huawei");
	private String name;

	CloudPlatformEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
