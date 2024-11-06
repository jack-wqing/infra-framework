package com.jindi.infra.grpc.server;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.jindi.infra.core.annotation.RPCInterface;
import com.jindi.infra.core.annotation.RPCService;
import com.jindi.infra.grpc.constant.ProtoGoogleJavaMapping;

import io.grpc.BindableService;
import lombok.extern.slf4j.Slf4j;

/**
 * 扫描RPCService注解修饰的bean，并将其注册为对外暴露的RPC服务
 */
@Slf4j
public class RPCServiceBeanPostProcessor implements BeanPostProcessor {

	private static final String SERVICE_NAME = "SERVICE_NAME";
	@Resource
	private GrpcServiceProxy grpcServiceProxy;

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class serviceClass = AopUtils.getTargetClass(bean);
		if (serviceClass.isAnnotationPresent(RPCService.class) && BindableService.class.isInstance(bean)) {
			cacheProtoGoogleJavaMapping(serviceClass);
			grpcServiceProxy.register(serviceClass, (BindableService) bean);
		}
		return bean;
	}

	/**
	 * 缓存ProtoGoogle类和service类的映射关系
	 */
	private void cacheProtoGoogleJavaMapping(Class serviceClass) {
		try {
			Class superclass = serviceClass.getSuperclass();
			while (superclass != null && superclass != Object.class) {
				if (superclass.isAnnotationPresent(RPCInterface.class)) {
					Field field = superclass.getDeclaredField(SERVICE_NAME);
					field.setAccessible(true);
					String protoGoogleClass = String.valueOf(field.get(serviceClass));
					ProtoGoogleJavaMapping.CLASS_NAME_MAPPING.put(protoGoogleClass, superclass.getName());
					return;
				}
				superclass = superclass.getSuperclass();
			}
		} catch (NoSuchFieldException noSuchFieldException) {
			// 老版本，插件生成的代码
		} catch (Throwable e) {
			log.error("缓存google->java包名映射失败");
		}
	}
}
