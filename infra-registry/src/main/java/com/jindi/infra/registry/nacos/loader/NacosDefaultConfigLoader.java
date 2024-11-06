package com.jindi.infra.registry.nacos.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

/**
 * @author changbo
 * @date 2021/7/16
 */
public class NacosDefaultConfigLoader implements IEnvDefaultConfigLoader {

	private static final String REGISTRY_DEFAULT_CONFIG = "/META-INF/registry.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, REGISTRY_DEFAULT_CONFIG);
	}
}
