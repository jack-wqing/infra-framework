package com.jindi.infra.trace.kafka.processor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.jindi.infra.trace.kafka.interceptor.InfraKafkaConsumerInterceptor;
import com.jindi.infra.trace.kafka.interceptor.InfraKafkaProducerInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfraKafkaBeanPostProcess implements BeanPostProcessor {

    private static final String SEPARATOR = ",";
    private static final String CONFIG_FIELD_NAME = "configs";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            // producer
            if (bean instanceof KafkaTemplate) {
                KafkaTemplate kafkaTemplate = (KafkaTemplate) bean;
                ProducerFactory producerFactory = kafkaTemplate.getProducerFactory();
                Map<String, Object> configs = fillInterceptor(producerFactory.getConfigurationProperties(), ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, InfraKafkaProducerInterceptor.class.getName());
                producerFactory.updateConfigs(configs);
            }
            // consumer
            if (bean instanceof AbstractKafkaListenerContainerFactory) {
                AbstractKafkaListenerContainerFactory containerFactory = (AbstractKafkaListenerContainerFactory) bean;
                ConsumerFactory consumerFactory = containerFactory.getConsumerFactory();
                if (consumerFactory instanceof DefaultKafkaConsumerFactory) {
                    Map<String, Object> configs = fillInterceptor(consumerFactory.getConfigurationProperties(), ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, InfraKafkaConsumerInterceptor.class.getName());
                    changeConsumerConfig((DefaultKafkaConsumerFactory)consumerFactory, configs);
                }
            }
        } catch (Exception e) {
            log.error("postProcessAfterInitialization error, beanName: {}", beanName, e);
        }
        return bean;
    }

    private void changeConsumerConfig(DefaultKafkaConsumerFactory consumerFactory, Map<String, Object> configs) throws Exception {
        Field field = consumerFactory.getClass().getDeclaredField(CONFIG_FIELD_NAME);
        field.setAccessible(true);
        field.set(consumerFactory, configs);
    }

    private Map<String, Object> fillInterceptor(Map<String, Object> configurationProperties, String key, String interceptorName) {
        Map<String, Object> configs = new HashMap<>(configurationProperties);
        String originInterceptors = (String) configurationProperties.get(key);
        if (StringUtils.isBlank(originInterceptors)) {
            configs.put(key, interceptorName);
            return configs;
        }
        if (!originInterceptors.contains(interceptorName)) {
            configs.put(key, interceptorName + SEPARATOR + originInterceptors);
            return configs;
        }
        return configs;
    }

}
