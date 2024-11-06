package com.jindi.infra.datasource.loader;


import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerPropertiesUtils;

import java.util.Properties;

public class CommonDataSourceConfigLoader implements IEnvDefaultConfigLoader {

    private static final String COMMON_DEFAULT_CONFIG = "/META-INF/common.properties";

    @Override
    public Properties loadDefaultConfig(String profile) {
        return InnerPropertiesUtils.load(profile, COMMON_DEFAULT_CONFIG);
    }
}