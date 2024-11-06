package com.jindi.infra.logger.encoder;

import static com.jindi.infra.logger.constant.LoggerConstants.*;

import java.nio.charset.Charset;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.core.env.Environment;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

/**
 * @author changbo Created on 2019-09-20
 */
public class FileConsolePatternLayoutEncoder extends PatternLayoutEncoder {

	public FileConsolePatternLayoutEncoder(Environment environment) {
		String showLineNumber = environment.getProperty(consoleShowLineNumberKey);
		if ("true".equals(showLineNumber)) {
			setPattern(fileConsoleLinePattern);
		} else {
			setPattern(fileConsoleNoLinePattern);
		}
		setCharset(Charset.forName("UTF-8"));
	}

	@Override
	public void start() {
		super.start();
	}
}
