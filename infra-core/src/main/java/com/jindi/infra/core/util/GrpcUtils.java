package com.jindi.infra.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class GrpcUtils {

	private static final int STACK_TRACE_MAX_LENGTH = 2000;

	public static String getStackTrace(Throwable e) {
		if (e == null) {
			return "";
		}
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		if (stringWriter.getBuffer().length() > STACK_TRACE_MAX_LENGTH) {
			return stringWriter.getBuffer().substring(0, STACK_TRACE_MAX_LENGTH);
		}
		return stringWriter.toString();
	}

	/**
	 * 提取服务端透传异常的message部分
	 *
	 * @param message
	 * @return
	 */
	public static String getExtractMessage(String message) {
		if (StringUtils.isBlank(message)) {
			return message;
		}
		String[] ss = StringUtils.split(message, "\n\t");
		if (ArrayUtils.isEmpty(ss)) {
			return message;
		}
		return ss[0];
	}
}
