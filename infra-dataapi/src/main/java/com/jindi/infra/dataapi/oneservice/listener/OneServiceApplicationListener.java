package com.jindi.infra.dataapi.oneservice.listener;

import java.util.*;
import java.util.stream.Collectors;

import com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts;
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
public class OneServiceApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final String INFRA_APOLLO_ONESERVICE_CONFIG = "infra-apollo-oneservice-config";
	private static final String APOLLO_BOOTSTRAP_NAMESPACES = "apollo.bootstrap.namespaces";
	private static final String SEPARATOR = ",";

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		if (environment instanceof StandardServletEnvironment) {
			Properties startup = new Properties();
			String namespaces = environment.getProperty(APOLLO_BOOTSTRAP_NAMESPACES);
			if (StringUtils.isBlank(namespaces)) {
				startup.put(APOLLO_BOOTSTRAP_NAMESPACES, OneServiceConsts.ONE_SERVICE_NAMESPACE);
			} else {
				Set<String> set = splitToSet(namespaces);
				set.add(OneServiceConsts.ONE_SERVICE_NAMESPACE);
				startup.put(APOLLO_BOOTSTRAP_NAMESPACES, StringUtils.join(set, SEPARATOR));
			}
			environment.getPropertySources().addFirst(new PropertiesPropertySource(INFRA_APOLLO_ONESERVICE_CONFIG, startup));
		}
	}

	private Set<String> splitToSet(String namespaces) {
		String[] ss = StringUtils.split(namespaces, SEPARATOR);
		if (ArrayUtils.isEmpty(ss)) {
			return new HashSet<>();
		} else {
			return Arrays.stream(ss).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toSet());
		}
	}
}
