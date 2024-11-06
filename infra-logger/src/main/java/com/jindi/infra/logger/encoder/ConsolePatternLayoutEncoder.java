package com.jindi.infra.logger.encoder;

import static com.jindi.infra.logger.constant.LoggerConstants.*;

import java.nio.charset.Charset;

import org.springframework.core.env.Environment;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

/**
 * @author changbo Created on 2019-09-20
 */
public class ConsolePatternLayoutEncoder extends PatternLayoutEncoder {

	public ConsolePatternLayoutEncoder(Environment environment) {
		String property = environment.getProperty(consoleShowLineNumberKey);
		if (property != null && property.equals("true")) {
			setPattern(consoleLinePattern);
		} else {
			setPattern(consoleNoLinePattern);
		}
		setCharset(Charset.forName("UTF-8"));
	}

	@Override
	public void start() {
		super.start();
	}
}
