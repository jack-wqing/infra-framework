package com.jindi.infra.feign.config;

import com.jindi.infra.feign.client.CatFeignClientDecorator;
import com.jindi.infra.feign.filter.FeignRequestFilter;
import com.jindi.infra.feign.interceptor.FeignCallInterceptor;
import feign.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAutoConfiguration {

    @Bean
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory) {
        return new LoadBalancerFeignClient(new CatFeignClientDecorator(new Client.Default(null, null)), cachingFactory,
                clientFactory);
    }

    @ConditionalOnMissingBean(name = "feignRequestFilter")
    @Bean(name = "feignRequestFilter")
    @ConditionalOnWebApplication
    public FeignRequestFilter feignRequestFilter() {
        return new FeignRequestFilter();
    }

    @ConditionalOnMissingBean(name = "feignCallInterceptor")
    @Bean(name = "feignCallInterceptor")
    public FeignCallInterceptor feignCallInterceptor() {
        return new FeignCallInterceptor();
    }
}

