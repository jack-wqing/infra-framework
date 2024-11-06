package com.jindi.infra.config;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.collect.Lists;
import com.jindi.infra.config.apollo.constant.SystemConsts;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogLevelApplicationContextInitializer
		implements
			ApplicationContextInitializer<ConfigurableApplicationContext>,
			Ordered {

	private static final String LOG_LEVEL_KEY_PREFIX = "logging.level.";

	@Override
	public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
		MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
		PropertySource<?> bootstrapPropertySource = propertySources
				.get(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
		// not exists or already in the first place
		if (bootstrapPropertySource == null) {
			return;
		}
		Collection<PropertySource<?>> sourceCollection = ((CompositePropertySource) bootstrapPropertySource)
				.getPropertySources();
		for (PropertySource<?> propertySource : Lists.newArrayList(sourceCollection)) {
			if (!Objects.equals(propertySource.getName(), SystemConsts.TSP_NAMESPACE)) {
				continue;
			}
			DefaultConfig source = (DefaultConfig) propertySource.getSource();
			Set<String> propertyNames = source.getPropertyNames();
			for (String propertyName : propertyNames) {
				if (StringUtils.isBlank(propertyName) || !propertyName.startsWith(LOG_LEVEL_KEY_PREFIX)) {
					continue;
				}
				String property = (String) propertySource.getProperty(propertyName);
				changeLogLevel(propertyName, property);
			}
		}
	}

	private void changeLogLevel(String logPath, String logLevel) {
		String loggerName = logPath.substring(LOG_LEVEL_KEY_PREFIX.length());
		log.info("更新:{} 日志级别：{}", loggerName, logLevel);
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger logger = loggerContext.getLogger(loggerName);
		logger.setLevel(Level.toLevel(logLevel));
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
