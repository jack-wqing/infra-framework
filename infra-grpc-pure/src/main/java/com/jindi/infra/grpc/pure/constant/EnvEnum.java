package com.jindi.infra.grpc.pure.constant;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public enum EnvEnum {
	DEV("dev"), TEST("test"), YUFA("yufa"), PROD("prod");
	public static EnvEnum DEFAULT_ENV = EnvEnum.DEV;
	private String name;

	EnvEnum(String name) {
		this.name = name;
	}

	public static EnvEnum fromName(String name) {
		if (StringUtils.isBlank(name)) {
			return DEFAULT_ENV;
		}
		for (EnvEnum envEnum : values()) {
			if (Objects.equals(envEnum.getName(), name)) {
				return envEnum;
			}
		}
		return DEFAULT_ENV;
	}

	public String getName() {
		return this.name;
	}
}
