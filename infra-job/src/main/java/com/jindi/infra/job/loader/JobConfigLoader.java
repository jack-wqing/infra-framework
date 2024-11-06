package com.jindi.infra.job.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class JobConfigLoader implements IEnvDefaultConfigLoader {

	private static final String JOB_DEFAULT_CONFIG = "/META-INF/job.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, JOB_DEFAULT_CONFIG);
	}
}
