package com.jindi.infra.trace.http.config;

import javax.servlet.DispatcherType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.jindi.infra.trace.http.HttpFilter;
import com.jindi.infra.trace.http.context.HttpTraceContext;

/**
 * trace filter autoConfiguration
 */
@Configuration
@ConditionalOnProperty(value = "trace.filter.enabled", matchIfMissing = true)
public class TraceHttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpFilter traceFilter() {
        return new HttpFilter();
    }

    @Bean
    public FilterRegistrationBean traceWebFilter(HttpFilter filter) {
        FilterRegistrationBean bean = new FilterRegistrationBean(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        bean.setDispatcherTypes(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD,
                DispatcherType.INCLUDE, DispatcherType.REQUEST);
        return bean;
    }

    @ConditionalOnMissingBean
    @Bean
    public HttpTraceContext getHttpTraceContext() {
        return new HttpTraceContext();
    }

}
