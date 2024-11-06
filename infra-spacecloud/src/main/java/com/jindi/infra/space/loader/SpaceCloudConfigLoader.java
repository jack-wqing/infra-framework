package com.jindi.infra.space.loader;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

import java.util.Properties;

public class SpaceCloudConfigLoader implements IEnvDefaultConfigLoader {

	private static final String SPACE_CLOUD_DEFAULT_CONFIG = "/META-INF/spacecloud.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, SPACE_CLOUD_DEFAULT_CONFIG);
	}
}
