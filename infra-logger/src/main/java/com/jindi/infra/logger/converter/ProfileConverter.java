package com.jindi.infra.logger.converter;

import static com.jindi.infra.logger.constant.LoggerConstants.profile;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author changbo Created on 2019-09-21
 */
public class ProfileConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		return profile;
	}
}
