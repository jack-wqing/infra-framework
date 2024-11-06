package com.jindi.infra.topology.feign.processor;

import com.jindi.infra.topology.model.ServiceCall;
import com.jindi.infra.topology.model.TopologyEsWriter;
import feign.Client;
import lombok.extern.slf4j.Slf4j;
import com.jindi.infra.topology.feign.client.TopologyFeignClient;
import com.jindi.infra.topology.feign.client.TopologyLoadBalanceFeignClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import javax.annotation.Resource;

@Slf4j
public class TopologyFeignContextBeanPostProcessor implements BeanPostProcessor {

    @Resource
    private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;
    @Resource
    private SpringClientFactory springClientFactory;
    @Resource
    private ServiceCall serviceCall;
    @Resource
    private TopologyEsWriter topologyEsWriter;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof LoadBalancerFeignClient && !(bean instanceof TopologyLoadBalanceFeignClient)) {
            return new TopologyLoadBalanceFeignClient((LoadBalancerFeignClient)bean, serviceCall, topologyEsWriter, cachingSpringLoadBalancerFactory, springClientFactory);
        }
        if (bean instanceof Client && !(bean instanceof TopologyFeignClient)) {
            return new TopologyFeignClient((Client)bean, serviceCall, topologyEsWriter);
        }
        return bean;
    }

}
