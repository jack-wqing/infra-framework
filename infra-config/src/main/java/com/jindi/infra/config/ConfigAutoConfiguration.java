package com.jindi.infra.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.jindi.infra.config.apollo.constant.SystemConsts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.util.ProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ConfigAutoConfiguration implements ApplicationContextAware {


    private ApplicationContext applicationContext;
    private static final String LOG_LEVEL_KEY_PREFIX = "logging.level.";
    private static final String DEFAULT_LOG_LEVEL = "info";
    private static final String RPC_PROPERTIES_BEAN_NAME = "rpc-com.jindi.infra.grpc.RpcProperties";
    private static final String RPC_PREFIX = "rpc.";

    @ApolloConfigChangeListener({ConfigConsts.NAMESPACE_APPLICATION, SystemConsts.TSP_NAMESPACE})
    public void apolloConfigChange(ConfigChangeEvent changeEvent) {
        boolean rpcPropertiesChange = false;
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("apollo config change - {}", change);
            if (key.startsWith(LOG_LEVEL_KEY_PREFIX)) {
                changeLogLevel(change);
            }
            if (!rpcPropertiesChange && key.startsWith(RPC_PREFIX)) {
                rpcPropertiesChange = true;
            }
        }
        if (rpcPropertiesChange) {
            rebind(RPC_PROPERTIES_BEAN_NAME);
        }
    }

    private void changeLogLevel(ConfigChange change) {
        String logLevel = change.getNewValue();
        String logPath = change.getPropertyName();
        if (change.getChangeType().equals(PropertyChangeType.DELETED)) {
            logLevel = DEFAULT_LOG_LEVEL;
        }
        if (StringUtils.isEmpty(logLevel)) {
            return;
        }
        String loggerName = logPath.substring(LOG_LEVEL_KEY_PREFIX.length());
        log.info("更新:{} 日志级别：{}", loggerName, logLevel);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(loggerName);
        logger.setLevel(Level.toLevel(logLevel));
    }

    private void rebind(String name) {
        if (this.applicationContext == null) {
            return;
        }
        try {
            Object bean = this.applicationContext.getBean(name);
            if (AopUtils.isAopProxy(bean)) {
                bean = ProxyUtils.getTargetObject(bean);
            }
            if (bean != null) {
                this.applicationContext.getAutowireCapableBeanFactory()
                    .destroyBean(bean);
                this.applicationContext.getAutowireCapableBeanFactory()
                    .initializeBean(bean, name);
            }
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }
}
