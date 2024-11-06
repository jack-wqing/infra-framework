package com.jindi.infra.reboot.eureka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;

import com.netflix.discovery.EurekaClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EurekaDestory {

    @Autowired(required = false)
    private EurekaAutoServiceRegistration eurekaRegistration;
    @Autowired(required = false)
    private EurekaClient eurekaClient;

    public void shutdownEureka() {
        try {
            if (eurekaRegistration != null) {
                eurekaRegistration.stop();
                log.info("shutdown EurekaAutoServiceRegistration 5s");
                Thread.sleep(5000L);
            }
            if (eurekaClient != null) {
                eurekaClient.shutdown();
                log.info("shutdown EurekaClient 50s");
                Thread.sleep(50000L);
            }
        } catch (Exception e) {
            log.error("eureka shutdown error", e);
        }
    }
}
