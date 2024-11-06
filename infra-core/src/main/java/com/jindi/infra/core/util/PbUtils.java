package com.jindi.infra.core.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PbUtils {

	private static final String NEW_BUILDER = "newBuilder";

	/**
	 * pb message 对象转 map
	 *
	 * @param message
	 * @return
	 */
	public static Map<String, Object> toMap(Message message) {
		if (message == null) {
			return null;
		}
		String data = toJSONString(message);
		return JSON.parseObject(data).getInnerMap();
	}

	/**
	 * pb message 对象转 java pojo
	 *
	 * @param message
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T toPojo(Message message, Class clazz) {
		if (message == null) {
			return null;
		}
		String data = toJSONString(message);
		return (T) InnerJSONUtils.parseObject(data, clazz);
	}

	/**
	 * pb message 对象转 json string 弊端，序列化后的数值都变为"1"字符串类型
	 *
	 * @param message
	 * @return
	 */
	public static String toJSONString(Message message) {
		if (message == null) {
			return null;
		}
		try {
			return com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields()
					.omittingInsignificantWhitespace().print(message);
		} catch (Throwable e) {
			log.error("toJSONString error", e);
		}
		return null;
	}

	/**
	 * pb message 对象转 json string 弊端，1. 如果数值为默认值，不会被包含 2. map类型 生成 [{"key": "name",
	 * "value": "CaoXin"}] 数组的形式
	 *
	 * @param message
	 * @return
	 */
	public static String toJSONStringV2(Message message) {
		if (message == null) {
			return null;
		}
		return JsonFormat.printToString(message);
	}

	/**
	 * java pojo 转 pb message 对象
	 *
	 * @param obj
	 * @param messageClass
	 * @param <T>
	 * @return
	 */
	public static <T> Message toMessage(Object obj, Class messageClass) {
		if (obj == null) {
			return null;
		}
		Message.Builder builder = null;
		try {
			builder = newBuilder(messageClass);
			return toMessage(obj, builder);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			log.error("", e);
		}
		return null;
	}

	/**
	 * java pojo 转 pb message 对象
	 *
	 * @param obj
	 * @param builder
	 * @param <T>
	 * @return
	 */
	public static <T> Message toMessage(Object obj, Message.Builder builder) {
		if (obj == null) {
			return null;
		}
		try {
			JsonFormat.merge(InnerJSONUtils.toJSONString(obj), builder);
			return builder.build();
		} catch (JsonFormat.ParseException e) {
			log.error("", e);
		}
		return null;
	}

	private static Message.Builder newBuilder(Class<?> messageClass)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return (Message.Builder) ((messageClass.getMethod(NEW_BUILDER)).invoke(null));
	}

	public static boolean isEmpty(Message message) {
		if (message == null) {
			return true;
		}
		return message.getSerializedSize() <= 0;
	}
}
