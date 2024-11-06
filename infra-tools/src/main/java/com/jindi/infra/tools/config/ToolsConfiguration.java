package com.jindi.infra.tools.config;


import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloProcessor;
import com.jindi.infra.tools.thread.DynamicThreadPoolConfigure;
import com.jindi.infra.tools.thread.DynamicThreadPoolListener;
import com.jindi.infra.tools.thread.ThreadPoolStaticMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolsConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public ThreadPoolStaticMetrics threadPoolStaticMetrics() {
        return new ThreadPoolStaticMetrics();
    }

    @ConditionalOnClass({ApolloProcessor.class, ConfigChangeEvent.class, ConfigChangeListener.class})
    @ConditionalOnMissingBean
    @Bean
    public DynamicThreadPoolListener dynamicThreadPoolListener() {
        return new DynamicThreadPoolListener();
    }

    @ConditionalOnClass({ApolloProcessor.class, ConfigChangeEvent.class, ConfigChangeListener.class})
    @ConditionalOnMissingBean
    @Bean
    public DynamicThreadPoolConfigure dynamicThreadPoolConfigure() {
        return new DynamicThreadPoolConfigure();
    }
}
