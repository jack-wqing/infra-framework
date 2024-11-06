package com.jindi.infra.trace.feign.processor;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import com.jindi.infra.trace.feign.client.InfraTraceFeignClient;
import com.jindi.infra.trace.feign.client.InfraTraceLoadBalanceFeignClient;
import com.jindi.infra.trace.model.TraceContext;

import feign.Client;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfraFeignContextBeanPostProcessor implements BeanPostProcessor {

    @Resource
    private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;
    @Resource
    private SpringClientFactory springClientFactory;
    @Resource
    private TraceContext traceContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof LoadBalancerFeignClient && !(bean instanceof InfraTraceLoadBalanceFeignClient)) {
            log.info("beanName: {}", beanName);
            return new InfraTraceLoadBalanceFeignClient((LoadBalancerFeignClient)bean, cachingSpringLoadBalancerFactory, springClientFactory, traceContext);
        }
        if (bean instanceof Client && !(bean instanceof InfraTraceFeignClient)) {
            log.info("beanName: {}", beanName);
            return new InfraTraceFeignClient((Client)bean, traceContext);
        }
        return bean;
    }


}
