package com.jindi.infra.mq.kafka.listener;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.jindi.infra.mq.kafka.constant.KafkaConsts;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaStartupApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final String INFRA_KAFKA_STARTUP_CONFIG = "infra-kafka-startup-config";
	private static final String SPRING_KAFKA_PRODUCER_PROPERTIES_INTERCEPTOR_CLASSES = "spring.kafka.producer.properties.interceptor.classes";
	private static final String SPRING_KAFKA_CONSUMER_PROPERTIES_INTERCEPTOR_CLASSES = "spring.kafka.consumer.properties.interceptor.classes";
	private static final String SEPARATOR = ",";

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		if (environment instanceof StandardServletEnvironment) {
			Properties startup = new Properties();
			configInterceptorClasses(SPRING_KAFKA_PRODUCER_PROPERTIES_INTERCEPTOR_CLASSES,
					KafkaConsts.PRODUCER_INTERCEPTORS, environment, startup);
			configInterceptorClasses(SPRING_KAFKA_CONSUMER_PROPERTIES_INTERCEPTOR_CLASSES,
					KafkaConsts.CONSUMER_INTERCEPTORS, environment, startup);
			for (Map.Entry entry : startup.entrySet()) {
				log.debug("kafka启动添加配置 key = {}, value = {}", entry.getKey(), entry.getValue());
			}
			environment.getPropertySources()
					.addFirst(new PropertiesPropertySource(INFRA_KAFKA_STARTUP_CONFIG, startup));
		}
	}

	/**
	 * properties 增加自定义配置
	 */
	private void configInterceptorClasses(String interceptorClassesKey, List<String> interceptorClassesValue,
			ConfigurableEnvironment environment, Properties startup) {
		String interceptorClasses = environment.getProperty(interceptorClassesKey);
		if (StringUtils.isBlank(interceptorClasses)) {
			startup.put(interceptorClassesKey, StringUtils.join(interceptorClassesValue, SEPARATOR));
			return;
		}
		Set<String> set;
		String[] ss = StringUtils.split(interceptorClasses, SEPARATOR);
		if (ArrayUtils.isEmpty(ss)) {
			set = new HashSet<>();
		} else {
			set = Arrays.stream(ss).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toSet());
		}
		for (String interceptorClass : interceptorClassesValue) {
			set.add(interceptorClass);
		}
		startup.put(interceptorClassesKey, StringUtils.join(set, SEPARATOR));
	}
}
