package com.jindi.infra.logger;

import com.jindi.infra.logger.config.TycLogDataBeanPostProcessor;
import com.jindi.infra.logger.elasticsearch.ElasticSearchWriter;
import com.jindi.infra.logger.metrics.AsyncLoggerQueueMetricsBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public ElasticSearchWriter elasticSearchWriter() {
        return new ElasticSearchWriter();
    }

    @ConditionalOnMissingBean
    @Bean
    public AsyncLoggerQueueMetricsBuilder asyncLoggerQueueMetricsBuilder() {
        return new AsyncLoggerQueueMetricsBuilder();
    }

    @ConditionalOnMissingBean
    @Bean
    public TycLogDataBeanPostProcessor tycLogDataBeanPostProcessor() {
        return new TycLogDataBeanPostProcessor();
    }
}
