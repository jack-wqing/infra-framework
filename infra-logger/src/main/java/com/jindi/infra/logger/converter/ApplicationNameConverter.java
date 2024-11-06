package com.jindi.infra.logger.converter;

import static com.jindi.infra.logger.constant.LoggerConstants.applicationName;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author changbo Created on 2019-09-21
 */
public class ApplicationNameConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		return applicationName;
	}
}
