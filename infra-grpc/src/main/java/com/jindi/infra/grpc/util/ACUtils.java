package com.jindi.infra.grpc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ACUtils implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	public static <T> T getBean(Class<T> clazz) {
		try {
			return applicationContext.getBean(clazz);
		} catch (Throwable e) {
			log.debug("", e);
		}
		return null;
	}

	public static <T> List<T> getBeansOfType(Class<T> clazz) {
		try {
			Map<String, T> beans = applicationContext.getBeansOfType(clazz);
			if (CollectionUtils.isEmpty(beans)) {
				return Collections.emptyList();
			}
			return new ArrayList<>(beans.values());
		} catch (Throwable e) {
			log.debug("", e);
		}
		return Collections.emptyList();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ACUtils.applicationContext = applicationContext;
	}
}
