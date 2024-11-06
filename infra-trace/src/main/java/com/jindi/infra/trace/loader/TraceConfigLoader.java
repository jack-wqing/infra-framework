package com.jindi.infra.trace.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class TraceConfigLoader implements IEnvDefaultConfigLoader {

	private static final String TRACE_DEFAULT_CONFIG = "/META-INF/trace.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, TRACE_DEFAULT_CONFIG);
	}
}
