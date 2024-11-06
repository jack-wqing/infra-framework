package com.jindi.infra.config.apollo.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

/**
 * @author changbo
 * @date 2021/7/16
 */
public class ApolloDefaultConfigLoader implements IEnvDefaultConfigLoader {

	private static final String APOLLO_DEFAULT_CONFIG = "/META-INF/config.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, APOLLO_DEFAULT_CONFIG);
	}
}
