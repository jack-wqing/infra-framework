package com.zspace.infra.metrics.thread;


import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import com.jindi.infra.tools.thread.ThreadPoolStaticMetrics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TomcatThreadReporter implements ApplicationListener<WebServerInitializedEvent>, ApplicationContextAware {

    private static String TOMCAT_THREAD_POOL = "tomcat-thread-pool";

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
        try {
            if (initialized.compareAndSet(false, true)) {
                initialize();
            }
        } catch (Exception e) {
            log.error("Init web server thread pool failed.", e);
        }
    }

    private void initialize() {
        if (!(applicationContext instanceof WebServerApplicationContext)) {
            return;
        }
        WebServer webServer = ((WebServerApplicationContext) applicationContext).getWebServer();
        if (!(webServer instanceof TomcatWebServer)) {
            return;
        }
        TomcatWebServer tomcatWebServer = (TomcatWebServer) webServer;
        Executor executor = tomcatWebServer.getTomcat().getConnector().getProtocolHandler().getExecutor();
        if (!(executor instanceof ThreadPoolExecutor)) {
            return;
        }
        ThreadPoolStaticMetrics.bindTo(TOMCAT_THREAD_POOL, (ThreadPoolExecutor) executor);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
