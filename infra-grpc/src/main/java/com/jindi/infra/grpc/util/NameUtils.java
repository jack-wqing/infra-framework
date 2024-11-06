package com.jindi.infra.grpc.util;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.grpc.MethodDescriptor;

public class NameUtils {

	public static String getMethodName(String title, String serviceClass, Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append(StringUtils.substringAfterLast(serviceClass, '.')).append(".").append(method.getName())
				.append("(");
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (ArrayUtils.isEmpty(parameterTypes)) {
			sb.append(")");
			return sb.toString();
		}
		for (Class<?> parameterType : parameterTypes) {
			if (StringUtils.contains(parameterType.getTypeName(), ".")) {
				sb.append(StringUtils.substringAfterLast(parameterType.getTypeName(), '.'));
			} else {
				sb.append(parameterType.getTypeName());
			}
			sb.append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), ")");
		return sb.toString();
	}

	public static <ReqT, RespT> String getMethodName(String title, MethodDescriptor<ReqT, RespT> methodDescriptor) {
		return String.format("%s:%s()", title, methodDescriptor.getFullMethodName());
	}

	public static <ReqT, RespT> String getSimpleMethodName(MethodDescriptor<ReqT, RespT> methodDescriptor) {
		String serviceName = methodDescriptor.getServiceName();
		String methodName = methodDescriptor.getBareMethodName();
		return StringUtils.substringAfterLast(serviceName, ".") + "." + methodName;
	}
}
