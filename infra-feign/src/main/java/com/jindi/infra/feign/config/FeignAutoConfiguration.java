package com.jindi.infra.feign.config;

import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import com.jindi.infra.feign.client.CatFeignClientDecorator;
import com.jindi.infra.feign.client.FeignClientMultiProtocolClientDecorator;
import com.jindi.infra.feign.client.SentinelFeignClientDecorator;
import com.jindi.infra.feign.filter.FeignRequestFilter;
import com.jindi.infra.feign.filter.FeignServerMultiProtocolFilter;
import com.jindi.infra.feign.interceptor.FeignCallInterceptor;
import com.jindi.infra.core.aspect.MultiProtocolServerInterceptor;
import com.jindi.infra.feign.interceptor.FeignCallMultiProtocolInterceptor;
import com.jindi.infra.feign.processor.FeignClientPostProcessor;
import feign.Client;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FeignAutoConfiguration {

    @ConditionalOnProperty(name = "feign.defaulthttpclient.enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory, ObjectProvider<List<MultiProtocolClientInterceptor>> clientInterceptorList) {
        return new LoadBalancerFeignClient(new FeignClientMultiProtocolClientDecorator(new SentinelFeignClientDecorator(new CatFeignClientDecorator(new Client.Default(null, null))), clientInterceptorList), cachingFactory,
                clientFactory);
    }

    @ConditionalOnProperty(name = "feign.defaulthttpclient.enabled", havingValue = "false")
    @Bean
    public FeignClientPostProcessor feignClientPostProcessor(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory, ObjectProvider<List<MultiProtocolClientInterceptor>> clientInterceptorList) {
        return new FeignClientPostProcessor(cachingFactory, clientFactory, clientInterceptorList);
    }

    @ConditionalOnMissingBean(name = "feignRequestFilter")
    @Bean(name = "feignRequestFilter")
    public FeignRequestFilter feignRequestFilter() {
        return new FeignRequestFilter();
    }

    @ConditionalOnMissingBean(name = "feignCallInterceptor")
    @Bean(name = "feignCallInterceptor")
    public FeignCallInterceptor feignCallInterceptor() {
        return new FeignCallInterceptor();
    }

    @ConditionalOnBean(type = {"com.jindi.infra.core.aspect.MultiProtocolClientInterceptor"})
    @ConditionalOnMissingBean(name = "feignCallMultiProtocolInterceptor")
    @Bean(name = "feignCallMultiProtocolInterceptor")
    public FeignCallMultiProtocolInterceptor feignCallMultiProtocolInterceptor(List<MultiProtocolClientInterceptor> interceptorList) {
        return new FeignCallMultiProtocolInterceptor(interceptorList);
    }

    @ConditionalOnBean(type = {"com.jindi.infra.core.aspect.MultiProtocolServerInterceptor"})
    @ConditionalOnMissingBean
    @Bean
    public FeignServerMultiProtocolFilter feignServerMultiProtocolFilter(List<MultiProtocolServerInterceptor> interceptorList) {
        return new FeignServerMultiProtocolFilter(interceptorList);
    }
}

