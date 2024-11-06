package com.jindi.infra.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InnerJSONUtils {


	/**
	 * 对象转JSON字符串
	 *
	 * @param obj
	 *            对象
	 * @return
	 */
	public static String toJSONString(Object obj) {
		if (obj == null) {
			return null;
		}
		return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
	}

	/**
	 *
	 */
	public static String toJSONString(Set<Class> classes) {
		if (!CollectionUtils.isEmpty(classes)) {
			List<String> names = classes.stream().map(c -> c.getName()).collect(Collectors.toList());
			return toJSONString(names);
		}
		return null;
	}

	/**
	 * JSON字符串转POJO对象
	 *
	 * @param data
	 *            JSON字符串
	 * @param clazz
	 *            POJO类
	 * @param <T>
	 * @return
	 */
	public static <T> T parseObject(String data, Class<T> clazz) {
		if (data == null || clazz == null) {
			return null;
		}
		return JSON.parseObject(data, clazz);
	}

	/**
	 * JSON字符串转POJO对象
	 *
	 * @param data
	 *            JSON字符串
	 * @param type
	 *            POJO类
	 * @param <T>
	 * @return
	 */
	public static <T> T parseObject(String data, Type type) {
		if (data == null || type == null) {
			return null;
		}
		return JSON.parseObject(data, type);
	}

	public static Map<String, Object> parseMap(String data) {
		if (StringUtils.isBlank(data)) {
			return Collections.emptyMap();
		}
		try {
			return new HashMap<>(JSON.parseObject(data).getInnerMap());
		} catch (Throwable e) {
			return Collections.emptyMap();
		}
	}

	/**
	 * JSON字符串转POJO对象
	 *
	 * @param data JSON字符串
	 * @param clazz POJO类
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> parseArray(String data, Class<T> clazz) {
		if (data == null || clazz == null) {
			return null;
		}
		return JSON.parseArray(data, clazz);
	}
}
