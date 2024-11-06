package com.jindi.infra.dataapi.oneservice.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class OneServiceConfigLoader implements IEnvDefaultConfigLoader {

	private static final String ONE_SERVICE_DEFAULT_CONFIG = "/META-INF/oneservice.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, ONE_SERVICE_DEFAULT_CONFIG);
	}
}
