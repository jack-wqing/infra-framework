package com.jindi.infra.common.env;

import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.loader.IEnvDefaultConfigLoader;
import com.jindi.infra.common.util.InnerEnvironmentUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfraEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String applicationName = environment.getProperty(CommonConstant.APPLICATION_NAME);
		if (StringUtils.isBlank(applicationName)) {
			return;
		}
		Properties properties = loadProperties(environment);
		if (properties == null || properties.isEmpty()) {
			return;
		}
		environment.getPropertySources().addLast(new PropertiesPropertySource("infra-default-config", properties));
	}

	private Properties loadProperties(ConfigurableEnvironment environment) {
		Properties allProperties = new Properties();
		String profile = InnerEnvironmentUtils.getEnv(environment);
		ServiceLoader<IEnvDefaultConfigLoader> serviceLoader = ServiceLoader.load(IEnvDefaultConfigLoader.class);
		Iterator<IEnvDefaultConfigLoader> it = serviceLoader.iterator();
		while (it.hasNext()) {
			IEnvDefaultConfigLoader loader = it.next();
			try {
				Properties properties = loader.loadDefaultConfig(profile);
				if (properties != null && !properties.isEmpty()) {
					properties.forEach((k, v) -> allProperties.put(k, v));
				}
			} catch (Exception e) {
				log.error("load properties error, className: {}", loader.getClass().getName(), e);
			}
		}
		return allProperties;
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE + 1000;
	}
}
