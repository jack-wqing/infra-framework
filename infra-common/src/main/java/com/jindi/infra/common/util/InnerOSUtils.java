package com.jindi.infra.common.util;

import org.apache.commons.lang3.StringUtils;

public class InnerOSUtils {

	/**
	 * 系统是否为linux
	 *
	 * @return
	 */
	public static Boolean isLinux() {
		String os = System.getProperty("os.name");
		if (StringUtils.isBlank(os)) {
			return false;
		}
		return StringUtils.contains(os.toLowerCase(), "linux");
	}
}
