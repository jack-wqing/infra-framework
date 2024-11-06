package com.jindi.infra.feign.processor;


import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import com.jindi.infra.feign.client.CatFeignClientDecorator;
import com.jindi.infra.feign.client.FeignClientMultiProtocolClientDecorator;
import com.jindi.infra.feign.client.SentinelFeignClientDecorator;
import feign.Client;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class FeignClientPostProcessor implements BeanPostProcessor {

    private CachingSpringLoadBalancerFactory cachingFactory;
    private SpringClientFactory clientFactory;
    private ObjectProvider<List<MultiProtocolClientInterceptor>> interceptorList;

    public FeignClientPostProcessor(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory, ObjectProvider<List<MultiProtocolClientInterceptor>> clientInterceptorList) {
        this.cachingFactory = cachingFactory;
        this.clientFactory = clientFactory;
        this.interceptorList = clientInterceptorList;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Client) {
            return wrapper((Client) bean);
        }
        return bean;
    }

    private Client wrapper(Client client) {
        if (client instanceof LoadBalancerFeignClient) {
            client = ((LoadBalancerFeignClient) client).getDelegate();
        }
        if (client instanceof FeignBlockingLoadBalancerClient) {
            client = ((FeignBlockingLoadBalancerClient) client).getDelegate();
        }
        return new LoadBalancerFeignClient(new FeignClientMultiProtocolClientDecorator(new SentinelFeignClientDecorator(
                new CatFeignClientDecorator(client)), interceptorList), cachingFactory, clientFactory);
    }
}
