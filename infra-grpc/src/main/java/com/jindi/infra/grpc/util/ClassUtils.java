package com.jindi.infra.grpc.util;

/**
 * 类型工具类
 */
public class ClassUtils {

	/**
	 * 根据类型名获取class类，tip 增强点，具有缓存能力
	 *
	 * @param name
	 *            类型名
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class forName(String name) throws ClassNotFoundException {
		return org.apache.commons.lang3.ClassUtils.getClass(org.springframework.util.ClassUtils.getDefaultClassLoader(),
				name);
	}
}
