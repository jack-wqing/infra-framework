package com.jindi.infra.tools.mdc;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import com.google.gson.reflect.TypeToken;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.tools.constant.MDCConstant;

/**
 * 自定义MDC 用于系统自定义索引，统一封装到customKV中
 *
 * @author changbo
 */
public class TycMDC {

	/**
	 * 保存自定义索引字段和值 为了防止出现同一个字段值类型前后不一致而导致的es类型对应失败，这里统一定义为String
	 *
	 * @param pass
	 *            是否向下游服务透传
	 */
	public static void addKV(String key, String value, boolean pass) {
		addLogCustomKV(key, value);
		if (pass) {
			addPassCustomKV(key, value);
		}
	}

	private static void addLogCustomKV(String key, String value) {
		String s = MDC.get(MDCConstant.CUSTOM_KV);
		if (StringUtils.isBlank(s)) {
			Map<String, String> map = new HashMap<>();
			map.put(key, value);
			MDC.put(MDCConstant.CUSTOM_KV, InnerJSONUtils.toJSONString(map));
		} else {
			Map<String, Object> map = InnerJSONUtils.parseObject(s, new TypeToken<HashMap<String, String>>() {
			}.getType());
			map.put(key, value);
			MDC.put(MDCConstant.CUSTOM_KV, InnerJSONUtils.toJSONString(map));
		}
	}

	private static void addPassCustomKV(String key, String value) {
		String s = MDC.get(MDCConstant.PASS_CUSTOM_KV);
		if (StringUtils.isBlank(s)) {
			Map<String, String> map = new HashMap<>();
			map.put(key, value);
			MDC.put(MDCConstant.PASS_CUSTOM_KV, InnerJSONUtils.toJSONString(map));
		} else {
			Map<String, Object> map = InnerJSONUtils.parseObject(s, new TypeToken<HashMap<String, String>>() {
			}.getType());
			map.put(key, value);
			MDC.put(MDCConstant.PASS_CUSTOM_KV, InnerJSONUtils.toJSONString(map));
		}
	}

	public static void innerCover(String json) {
		MDC.put(MDCConstant.CUSTOM_KV, json);
		MDC.put(MDCConstant.PASS_CUSTOM_KV, json);
	}

	/**
	 * 获取MDC值
	 */
	public static String getKV(String key) {
		String s = MDC.get(MDCConstant.CUSTOM_KV);
		if (StringUtils.isNotBlank(s)) {
			Map<String, String> map = InnerJSONUtils.parseObject(s, new TypeToken<HashMap<String, String>>() {
			}.getType());
			return map.get(key);
		}
		return null;
	}

	/**
	 * 获取MDC值
	 */
	public static String getAllPassCustomKv() {
		return MDC.get(MDCConstant.PASS_CUSTOM_KV);
	}

	/**
	 * 保存访问用户id到mdc
	 */
	public static void putUserId(String userId) {
		MDC.put(MDCConstant.USER_ID, userId);
	}

	/**
	 * 获取用户id
	 */
	public static String getUserId() {
		return MDC.get(MDCConstant.USER_ID);
	}
}
