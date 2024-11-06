package com.jindi.infra.logger.loader;

import org.springframework.core.env.Environment;

import com.jindi.infra.common.loader.ILogAppenderLoader;
import com.jindi.infra.logger.encoder.ConsolePatternLayoutEncoder;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import lombok.extern.slf4j.Slf4j;

/**
 * @author changbo Created on 2020-04-23
 */
@Slf4j
public class ConsoleAppenderLoader implements ILogAppenderLoader {

	@Override
	public void load(LoggerContext loggerContext, Environment environment) {
		Logger root = loggerContext.getLogger("ROOT");
		root.detachAppender("CONSOLE");
		ConsoleAppender consoleAppender = new ConsoleAppender();
		ConsolePatternLayoutEncoder encoder = new ConsolePatternLayoutEncoder(environment);
		encoder.setContext(loggerContext);
		encoder.start();
		consoleAppender.setEncoder(encoder);
		consoleAppender.setContext(loggerContext);
		consoleAppender.setName("CONSOLE");
		root.addAppender(consoleAppender);
		consoleAppender.start();
	}
}
