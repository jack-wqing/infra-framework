package com.jindi.infra.feign.loader;

import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

import java.util.Properties;

/**
 * @author limaozhan
 */
public class SentinelDefaultConfigLoader implements IEnvDefaultConfigLoader {

    private static final String APOLLO_DEFAULT_CONFIG = "/META-INF/feign.properties";

    @Override
    public Properties loadDefaultConfig(String profile) {
        return InnerPropertiesUtils.load(profile, APOLLO_DEFAULT_CONFIG);
    }
}
