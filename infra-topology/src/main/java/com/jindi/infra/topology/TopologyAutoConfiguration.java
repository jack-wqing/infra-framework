package com.jindi.infra.topology;

import com.jindi.infra.logger.elasticsearch.ElasticSearchWriter;
import com.jindi.infra.topology.model.ServiceCall;
import com.jindi.infra.topology.model.TopologyEsWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopologyAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public ElasticSearchWriter elasticSearchWriter() {
        return new ElasticSearchWriter();
    }

    @ConditionalOnMissingBean
    @Bean
    public ServiceCall serviceCall() {
        return new ServiceCall();
    }

    @ConditionalOnMissingBean
    @Bean
    public TopologyEsWriter topologyEsWriter() {
        return new TopologyEsWriter();
    }
}
