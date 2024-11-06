package com.jindi.infra.governance.loader;

import java.util.Properties;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

public class GovernanceConfigLoader implements IEnvDefaultConfigLoader {

	private static final String GOVERNANCE_DEFAULT_CONFIG = "/META-INF/governance.properties";

	@Override
	public Properties loadDefaultConfig(String profile) {
		return InnerPropertiesUtils.load(profile, GOVERNANCE_DEFAULT_CONFIG);
	}
}
