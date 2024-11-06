package com.jindi.infra.topology.http.config;

import com.jindi.infra.topology.http.filter.TopologyHttpFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.DispatcherType;

@Configuration
public class TopologyHttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TopologyHttpFilter topologyHttpFilter() {return new TopologyHttpFilter();}

    @Bean
    public FilterRegistrationBean topologyWebFilter(TopologyHttpFilter filter) {
        FilterRegistrationBean bean = new FilterRegistrationBean(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        bean.setDispatcherTypes(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD,
                DispatcherType.INCLUDE, DispatcherType.REQUEST);
        return bean;
    }

}
