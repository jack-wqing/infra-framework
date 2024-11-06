package com.jindi.infra.logger.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class LoggerConfigLoader implements IEnvDefaultConfigLoader {

	private static final String LOGGER_DEFAULT_CONFIG = "/META-INF/logger.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, LOGGER_DEFAULT_CONFIG);
	}
}
