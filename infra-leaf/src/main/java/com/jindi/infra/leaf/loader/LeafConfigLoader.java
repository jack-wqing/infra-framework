package com.jindi.infra.leaf.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class LeafConfigLoader implements IEnvDefaultConfigLoader {

	private static final String LEAF_DEFAULT_CONFIG = "/META-INF/leaf.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, LEAF_DEFAULT_CONFIG);
	}
}
