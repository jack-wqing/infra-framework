package com.jindi.infra.logger.converter;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author changbo Created on 2019-09-21
 */
public class LineConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		StackTraceElement[] cda = event.getCallerData();
		if (cda != null && cda.length > 0) {
			return Integer.toString(cda[0].getLineNumber());
		} else {
			return "-1";
		}
	}
}
