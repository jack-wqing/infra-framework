package com.jindi.infra.trace.kafka.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import com.jindi.infra.trace.kafka.processor.InfraKafkaBeanPostProcess;

@Configuration
@ConditionalOnClass({KafkaTemplate.class, AbstractKafkaListenerContainerFactory.class})
public class TraceKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InfraKafkaBeanPostProcess infraKafkaProducerBeanPostProcess() {
        return new InfraKafkaBeanPostProcess();
    }
}
