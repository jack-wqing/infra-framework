package com.jindi.infra.metrics.thread;


import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadAutoConfiguration {

    @Configuration
    @AutoConfigureAfter(ThreadPoolInitReporter.class)
    public class AdapterThreadAutoConfiguration {
        @ConditionalOnMissingBean
        @ConditionalOnWebApplication
        @Bean
        public TomcatThreadReporter tomcatThreadReporter() {
            return new TomcatThreadReporter();
        }
    }


    @ConditionalOnMissingBean
    @Bean
    public ThreadPoolInitReporter threadPoolInitReporter(List<ThreadPoolExecutor> threadPoolExecutorList, MeterRegistry meterRegistry) {
        return new ThreadPoolInitReporter(threadPoolExecutorList, meterRegistry);
    }

}
