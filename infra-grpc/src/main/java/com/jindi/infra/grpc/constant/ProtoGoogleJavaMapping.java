package com.jindi.infra.grpc.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * ProtoGoogle类和service类的映射关系
 */
public class ProtoGoogleJavaMapping {

	/**
	 * 缓存ProtoGoogle类 -> service类的映射关系
	 * google.protobuf.com.jindi.infra.demo.server.RemoteUserService ->
	 * com.jindi.infra.demo.server.sdk.proto.RemoteUserService
	 */
	public static final Map<String, String> CLASS_NAME_MAPPING = new HashMap<>();

	/**
	 * 缓存ProtoGoogle类/方法 -> service类/方法的映射关系
	 * google.protobuf.com.jindi.infra.demo.server.RemoteUserService/GetById ->
	 * com.jindi.infra.demo.server.sdk.proto.RemoteUserService/getById
	 */
	private static Map<String, String> METHOD_NAME_MAPPING = new ConcurrentHashMap<>();

	public static String convert(String fullMethodName) {
		String value = METHOD_NAME_MAPPING.get(fullMethodName);
		if (StringUtils.isNotBlank(value)) {
			return value;
		}
		if (fullMethodName.endsWith("/ping")) {
			return fullMethodName;
		}
		int index = fullMethodName.indexOf("/");
		String protoClassName = fullMethodName.substring(0, index);
		String serviceClassName = ProtoGoogleJavaMapping.CLASS_NAME_MAPPING.get(protoClassName);
		if (StringUtils.isBlank(serviceClassName)) {
			return fullMethodName;
		}
		char methodFirstWord = fullMethodName.charAt(index + 1);
		value = serviceClassName + "/" + String.valueOf(methodFirstWord).toLowerCase()
				+ fullMethodName.substring(index + 2);
		METHOD_NAME_MAPPING.put(fullMethodName, value);
		return value;
	}

}
