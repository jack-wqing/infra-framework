package com.jindi.infra.registry.eureka;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

import javax.annotation.Resource;

/**
 * 解决apollo变更触发代码中refreshAll，导致eureka掉线
 */
public class RemoveRefreshAllApplicationRunner implements ApplicationRunner {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private ApplicationEventMulticaster multicaster;

    private static final String EUREKA_REFRESHER_BEAN_NAME = "org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration$EurekaClientConfigurationRefresher";

    @Override
    public void run(ApplicationArguments args) {
        try {
            Object refresherBean = applicationContext.getBean(EUREKA_REFRESHER_BEAN_NAME);
            multicaster.removeApplicationListener((ApplicationListener<?>) refresherBean);
            multicaster.removeApplicationListenerBean(EUREKA_REFRESHER_BEAN_NAME);
        } catch (Exception e) {
            // 异常忽略
        }
    }
}
