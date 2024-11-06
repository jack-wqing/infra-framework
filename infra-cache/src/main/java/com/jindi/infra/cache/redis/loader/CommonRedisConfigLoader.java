package com.jindi.infra.cache.redis.loader;


import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

import java.util.Properties;

public class CommonRedisConfigLoader implements IEnvDefaultConfigLoader {

    private static final String COMMON_DEFAULT_CONFIG = "/META-INF/commonredis.properties";

    @Override
    public Properties loadDefaultConfig(String profile) {
        return InnerPropertiesUtils.load(profile, COMMON_DEFAULT_CONFIG);
    }
}