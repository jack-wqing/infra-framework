package com.jindi.infra.tools.util;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JSONUtils {

	private static final Gson GSON = new Gson();

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
		return GSON.toJson(obj);
	}

	/**
	 *
	 */
	public static String toJSONString(Set<Class> classes) {
		if (!CollectionUtils.isEmpty(classes)) {
			List<String> names = classes.stream().map(c -> c.getName()).collect(Collectors.toList());
			return GSON.toJson(names);
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
		return GSON.fromJson(data, clazz);
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
		return GSON.fromJson(data, type);
	}

	@Deprecated
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
}
