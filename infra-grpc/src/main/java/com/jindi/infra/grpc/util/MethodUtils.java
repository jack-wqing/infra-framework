package com.jindi.infra.grpc.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessageV3;
import com.jindi.infra.core.annotation.RPCMethod;

import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MethodUtils {

	private static final String ASYNC_SUFFIX = "Async";
	private static Map<Method, Boolean> cachedAsyncRemoteMethod = new ConcurrentHashMap<>();
	private static Map<Method, Boolean> cachedRemoteMethod = new ConcurrentHashMap<>();
	private static Map<Method, Method> cachedMethodMapping = new ConcurrentHashMap<>();
	private static Map<Method, MethodDescriptor> methodDescriptorCached = new ConcurrentHashMap<>();


	public static Boolean isRemoteMethod(Method method) {
		return cachedRemoteMethod.computeIfAbsent(method, m -> {
			if (m == null) {
				return false;
			}
			if (!m.isAnnotationPresent(RPCMethod.class)) {
				return false;
			}
			Class<?>[] parameterTypes = m.getParameterTypes();
			if (parameterTypes.length != 1) {
				return false;
			}
			return GeneratedMessageV3.class.isAssignableFrom(parameterTypes[0]);
		});
	}

	public static Boolean isAsyncMethod(Method method) {
		return cachedAsyncRemoteMethod.computeIfAbsent(method, m -> {
			if (!isRemoteMethod(m)) {
				return false;
			}
			return StringUtils.endsWith(m.getName(), ASYNC_SUFFIX) && m.getName().length() > ASYNC_SUFFIX.length();
		});
	}

	public static Method getRemoteMethod(Class<BindableService> serviceClass, Method method) {
		return cachedMethodMapping.computeIfAbsent(method, m -> {
			if (!isRemoteMethod(m)) {
				return null;
			}
			String methodName = m.getName();
			if (isAsyncMethod(m)) {
				methodName = methodName.substring(0, methodName.length() - ASYNC_SUFFIX.length());
			}
			try {
				return serviceClass.getMethod(methodName, m.getParameterTypes());
			} catch (NoSuchMethodException e) {
				log.error("", e);
			}
			return null;
		});
	}

	public static MethodDescriptor getMethodDescriptor(Class<BindableService> serviceClass, Method method) {
		return methodDescriptorCached.computeIfAbsent(method, m -> {
			Class implBaseClass = serviceClass.getSuperclass();
			String grpcClass = StringUtils.substringBeforeLast(implBaseClass.getTypeName(), "$");
			StringBuilder sb = new StringBuilder();
			sb.append(Character.toUpperCase(m.getName().charAt(0)));
			if (m.getName().length() > 1) {
				sb.append(StringUtils.substring(m.getName(), 1));
			}
			try {
				Method methodDescriptor = ClassUtils.forName(grpcClass)
						.getDeclaredMethod(String.format("get%sMethod", sb));
				return (MethodDescriptor) methodDescriptor.invoke(null);
			} catch (Throwable e) {
				log.error("", e);
			}
			return null;
		});
	}
}
