package com.jindi.infra.common.constant;

import org.apache.commons.lang3.StringUtils;

/**
 * @author changbo
 * @date 2021/7/13
 */
public class LoggerPath {

	public static String BASE_LOG_PATH;

	static {
		BASE_LOG_PATH = System.getProperty("log_file_path");
		if (BASE_LOG_PATH == null) {
			String home = System.getenv("HOME");
			if (StringUtils.isNotBlank(home) && home.startsWith("/Users")) {
				BASE_LOG_PATH = home + "/log";
			} else {
				BASE_LOG_PATH = "/data/log";
			}
		}
		if (!BASE_LOG_PATH.endsWith("/")) {
			BASE_LOG_PATH = BASE_LOG_PATH + "/";
		}
	}
}
