package com.jindi.infra.common.loader;

import java.util.Properties;

/**
 * @author changbo
 * @date 2021/7/16
 */
public interface IEnvDefaultConfigLoader {

	Properties loadDefaultConfig(String profile);
}
