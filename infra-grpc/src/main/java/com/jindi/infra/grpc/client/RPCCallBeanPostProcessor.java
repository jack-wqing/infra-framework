package com.jindi.infra.grpc.client;

import java.lang.reflect.Field;

import com.jindi.infra.common.constant.RegionConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import com.jindi.infra.core.annotation.RPCCall;
import com.jindi.infra.core.annotation.RPCInterface;
import com.jindi.infra.grpc.constant.ProtoGoogleJavaMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * 扫描注解RPCCall修饰的字段，并为其织入代理对象
 */
@Slf4j
public class RPCCallBeanPostProcessor implements BeanPostProcessor {

	private static final String SERVICE_NAME = "SERVICE_NAME";
	@Autowired
	private GrpcClientProxy grpcClientProxy;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Class clazz = ClassUtils.getUserClass(bean);
		Field[] fields = clazz.getDeclaredFields();
		if (ArrayUtils.isEmpty(fields)) {
			return bean;
		}
		for (Field field : fields) {
			if (!field.isAnnotationPresent(RPCCall.class)) {
				continue;
			}
			try {
				cacheProtoGoogleJavaMapping(field.getType());
				RPCCall annotation = field.getDeclaredAnnotation(RPCCall.class);
				String region = annotation.region();
				int callTimeoutMillis = annotation.callTimeoutMillis();
				log.info("服务bean {}, 引入 {} RPC服务 分区 {}", beanName, field.getType(), region);
				if (RegionConstant.ALIYUN_REGION.equals(region)) {
					log.error("aliyun region已失效，请统一使用huawei region，使用方式 @RPCCall(region=\"huawei\")，类名："+field.getName());
					throw new RuntimeException("aliyun region已失效，请统一使用huawei region，使用方式 @RPCCall(region=\"huawei\")，类名：" + field.getName());
				}
				Object obj = grpcClientProxy.proxy(field.getType(), region, callTimeoutMillis);
				if (obj == null) {
					continue;
				}
				field.setAccessible(true);
				field.set(bean, obj);
			} catch (Throwable e) {
				log.error("", e);
				throw new BeansException("", e) {
				};
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 缓存ProtoGoogle类和service类的映射关系
	 */
	private void cacheProtoGoogleJavaMapping(Class fieldType) {
		Class serviceClass = fieldType;
		try {
			while (serviceClass != null && serviceClass != Object.class) {
				if (serviceClass.isAnnotationPresent(RPCInterface.class)) {
					Field field = serviceClass.getDeclaredField(SERVICE_NAME);
					field.setAccessible(true);
					String protoGoogleClass = String.valueOf(field.get(serviceClass));
					ProtoGoogleJavaMapping.CLASS_NAME_MAPPING.put(protoGoogleClass, serviceClass.getName());
					return;
				}
				serviceClass = serviceClass.getSuperclass();
			}
		} catch (NoSuchFieldException noSuchFieldException) {
			// 老版本，插件生成的代码
		} catch (Throwable e) {
			log.error("缓存google->java包名映射失败");
		}
	}
}
