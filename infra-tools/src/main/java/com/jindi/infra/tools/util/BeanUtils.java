package com.jindi.infra.tools.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BeanUtils {

    public static <T> T registerIfAbsent(BeanFactory beanFactory, String beanName, T obj) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            return obj;
        }
        ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)beanFactory;
        if (beanFactory.containsBean(beanName)) {
            log.debug("当前Bean已存在, 无法继续注册 {}", beanName);
            return (T) beanFactory.getBean(beanName);
        }
        bf.registerSingleton(beanName, obj);
        return obj;
    }

    public static Map<String, Object> getBeansWithAnnotation(BeanFactory beanFactory, Class<? extends Annotation> annotationType) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            return new HashMap<>();
        }
        ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)beanFactory;
        return bf.getBeansWithAnnotation(annotationType);
    }

    public static String[] getBeanNamesForType(BeanFactory beanFactory, Class<?> type) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            return null;
        }
        ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)beanFactory;
        return bf.getBeanNamesForType(type);
    }

}
