package com.jindi.infra.config.apollo.listener;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.jindi.infra.config.apollo.constant.SystemConsts;

import lombok.extern.slf4j.Slf4j;

/**
 * @author changbo
 * @date 2021/9/28
 */
@Slf4j
public class ApolloStartupApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final String INFRA_APOLLO_STARTUP_CONFIG = "infra-apollo-startup-config";
	private static final String APOLLO_BOOTSTRAP_ENABLED = "apollo.bootstrap.enabled";
	private static final String APOLLO_BOOTSTRAP_NAMESPACES = "apollo.bootstrap.namespaces";
	private static final String SEPARATOR = ",";

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		if (environment instanceof StandardServletEnvironment) {
			Properties startup = new Properties();
			startup.put(APOLLO_BOOTSTRAP_ENABLED, true);
			String namespace = environment.getProperty(APOLLO_BOOTSTRAP_NAMESPACES);
			if (StringUtils.isBlank(namespace)) {
				startup.put(APOLLO_BOOTSTRAP_NAMESPACES, SystemConsts.TSP_NAMESPACE);
			} else {
				Set<String> set;
				String[] ss = StringUtils.split(namespace, SEPARATOR);
				if (ArrayUtils.isEmpty(ss)) {
					set = new HashSet<>();
				} else {
					set = Arrays.stream(ss).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toSet());
				}
				set.add(SystemConsts.TSP_NAMESPACE);
				startup.put(APOLLO_BOOTSTRAP_NAMESPACES, StringUtils.join(set, SEPARATOR));
			}
			for (Map.Entry entry : startup.entrySet()) {
				log.debug("框架最终添加配置 key = {}, value = {}", entry.getKey(), entry.getValue());
			}
			environment.getPropertySources()
					.addFirst(new PropertiesPropertySource(INFRA_APOLLO_STARTUP_CONFIG, startup));
		}
	}
}
