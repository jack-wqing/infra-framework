package com.jindi.infra.common.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author changbo
 * @date 2021/7/16
 */
@Slf4j
public class InnerPropertiesUtils {

	public static Properties load(String profile, String config) {
		Properties defaultProperties = InnerPropertiesUtils.load(config);
		if (defaultProperties.isEmpty()) {
			return null;
		}
		String env = profile;
		Properties envProperties = new Properties();
		defaultProperties.forEach((o1, o2) -> {
			String key = (String) o1;
			if (StringUtils.startsWith(key, env)) {
				key = StringUtils.substring(key, env.length() + 1);
				envProperties.setProperty(key, String.valueOf(o2));
			}
		});
		return envProperties;
	}

	private static Properties load(String resource) {
		Properties properties = new Properties();
		try (InputStream inputStream = InnerPropertiesUtils.class.getResourceAsStream(resource)) {
			if (inputStream == null) {
				log.warn("infra common config resource = {} loading failure", resource);
				return properties;
			}
			properties.load(inputStream);
		} catch (Exception e) {
			log.error("PropertiesUtil load {} error ", resource, e);
		}
		return properties;
	}
}
