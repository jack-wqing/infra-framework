package com.jindi.infra.common.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.constant.EnvEnums;

/**
 * @author changbo
 * @date 2021/7/16
 */
public class InnerEnvironmentUtils {

	public static String getEnv(Environment environment) {
		String activeProfiles = environment.getProperty(CommonConstant.PROFILE);
		if (StringUtils.isBlank(activeProfiles)) {
			return EnvEnums.DEFAULT_ENV_ENUMS.getStandardEnv();
		}
		return getEnv(activeProfiles);
	}

	private static String getEnv(String activeProfiles) {
		String[] profiles = StringUtils.split(activeProfiles, ",");
		for (String profile : profiles) {
			if (StringUtils.isBlank(profile)) {
				continue;
			}
			EnvEnums envEnums = EnvEnums.getEnvEnumsByEnv(profile.trim());
			if (envEnums != null) {
				return envEnums.getStandardEnv();
			}
		}
		return EnvEnums.DEFAULT_ENV_ENUMS.getStandardEnv();
	}

	public static boolean isDev(String[] activeProfiles) {
		if (ArrayUtils.isEmpty(activeProfiles)) {
			return true;
		}
		for (String profile : activeProfiles) {
			if (EnvEnums.getEnvEnumsByEnv(profile) == EnvEnums.DEV) {
				return true;
			}
		}
		return false;
	}

	public static boolean isProd(String[] activeProfiles) {
		if (ArrayUtils.isEmpty(activeProfiles)) {
			return false;
		}
		for (String profile : activeProfiles) {
			if (EnvEnums.getEnvEnumsByEnv(profile) == EnvEnums.PROD) {
				return true;
			}
		}
		return false;
	}

	public static boolean isYufa(String[] activeProfiles) {
		if (ArrayUtils.isEmpty(activeProfiles)) {
			return false;
		}
		for (String profile : activeProfiles) {
			if (EnvEnums.getEnvEnumsByEnv(profile) == EnvEnums.YUFA) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTest(String[] activeProfiles) {
		if (ArrayUtils.isEmpty(activeProfiles)) {
			return false;
		}
		for (String profile : activeProfiles) {
			if (EnvEnums.getEnvEnumsByEnv(profile) == EnvEnums.TEST) {
				return true;
			}
		}
		return false;
	}
}
