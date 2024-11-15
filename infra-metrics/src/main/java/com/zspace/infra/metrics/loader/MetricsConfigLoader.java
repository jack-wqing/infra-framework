package com.zspace.infra.metrics.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class MetricsConfigLoader implements IEnvDefaultConfigLoader {

	private static final String METRICS_DEFAULT_CONFIG = "/META-INF/metrics.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, METRICS_DEFAULT_CONFIG);
	}
}
