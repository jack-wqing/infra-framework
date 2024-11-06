package com.jindi.infra.common.constant;

import java.util.Arrays;
import java.util.List;

public enum EnvEnums {
	DEV("开发环境", "dev", Arrays.asList(new String[]{"dev"})), TEST("测试环境", "test",
			Arrays.asList(new String[]{"test"})), YUFA("预发环境", "yufa",
					Arrays.asList(new String[]{"yufa", "pre"})), PROD("生产环境", "prod",
							Arrays.asList(new String[]{"prod", "pro", "online"})), PFASTER("pfaster环境", "pfaster",
									Arrays.asList(new String[]{"pfaster"}));

	public static final EnvEnums DEFAULT_ENV_ENUMS = DEV;
	private String name;
	private String standardEnv;
	private List<String> envs;

	EnvEnums(String name, String standardEnv, List<String> envs) {
		this.name = name;
		this.standardEnv = standardEnv;
		this.envs = envs;
	}

	public static EnvEnums getEnvEnumsByEnv(String env) {
		for (EnvEnums envEnums : values()) {
			if (envEnums.getEnvs().contains(env)) {
				return envEnums;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public String getStandardEnv() {
		return standardEnv;
	}

	public List<String> getEnvs() {
		return envs;
	}
}
