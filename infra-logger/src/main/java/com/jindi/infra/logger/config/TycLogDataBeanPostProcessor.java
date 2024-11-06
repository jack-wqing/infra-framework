package com.jindi.infra.logger.config;


import com.jindi.infra.logger.logger.TycLogDataInterface;
import com.jindi.infra.logger.logger.TycLogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class TycLogDataBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TycLogDataInterface) {
            TycLogger.addInterface((TycLogDataInterface) bean);
        }
        return bean;
    }
}
