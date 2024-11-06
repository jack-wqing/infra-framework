package com.jindi.infra.reboot.listener;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jindi.infra.common.util.InnerEnvironmentUtils;
import com.jindi.infra.reboot.constant.RebootConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;

import com.jindi.infra.grpc.lifecycle.GrpcServiceAutoRegistration;
import com.jindi.infra.reboot.dubbo.DubboDestory;
import com.jindi.infra.reboot.eureka.EurekaDestory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
public class UnawareBootListener implements SmartApplicationListener{

    private AtomicBoolean running = new AtomicBoolean(true);
    @Autowired(required = false)
    private GrpcServiceAutoRegistration nacosRegistration;
    @Autowired(required = false)
    private DubboDestory dubboDestory;
    @Autowired(required = false)
    private EurekaDestory eurekaDestory;
    @Autowired
    private Environment environment;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextClosedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (isSkip()) {
            log.warn("skip reboot for env: {}", Arrays.toString(environment.getActiveProfiles()));
            return;
        }
        if (running.get()) {
            synchronized (UnawareBootListener.class) {
                if (running.get()) {
                    shutdownReadinessCheck();
                    shutdownEureka();
                    shutdownNacos();
                    shutdownDubbo();
                    running.set(false);
                }
            }
        }
        log.warn("shutdown all finish ---------");
    }

    private void shutdownReadinessCheck() {
        RebootConstant.readinessCheck = false;
        log.warn("shutdown readiness check finish --------");
    }

    private void shutdownEureka() {
        if (eurekaDestory != null) {
            eurekaDestory.shutdownEureka();
            log.warn("shutdown eureka finish --------");
        }
    }

    private void shutdownNacos() {
        try {
            if (nacosRegistration != null) {
                nacosRegistration.stop();
                log.warn("shutdown GrpcServiceAutoRegistration 3s finish -------");
            }
        } catch (Exception e) {
            log.error("nacos shutdown error", e);
        }
    }

    private void shutdownDubbo() {
        try {
            if (dubboDestory != null) {
                dubboDestory.shutdownDubbo();
                log.info("shutdown DubboDestory 40s");
                Thread.sleep(40000L);
                log.warn("shutdown dubbo finish --------");
            }
        } catch (Exception e) {
            log.error("dubbo shutdown error", e);
        }
    }

    /**
     * 非线上环境跳过平滑启动，这里直接判断非线上环境，避免有的项目不使用prod启动
     */
    private boolean isSkip() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (InnerEnvironmentUtils.isYufa(activeProfiles)) {
            return true;
        }
        if (InnerEnvironmentUtils.isTest(activeProfiles)) {
            return true;
        }
        if (InnerEnvironmentUtils.isDev(activeProfiles)) {
            return true;
        }
        return false;
    }

    // eureka是0，这里设置成-100
    @Override
    public int getOrder() {
        return -100;
    }
}
