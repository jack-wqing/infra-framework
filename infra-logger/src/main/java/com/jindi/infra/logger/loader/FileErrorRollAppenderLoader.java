package com.jindi.infra.logger.loader;

import static com.jindi.infra.common.constant.LoggerPath.BASE_LOG_PATH;
import static com.jindi.infra.logger.constant.LoggerConstants.applicationName;
import static com.jindi.infra.logger.constant.LoggerConstants.loggerName;

import org.springframework.core.env.Environment;

import com.jindi.infra.common.loader.ILogAppenderLoader;
import com.jindi.infra.logger.encoder.FileConsolePatternLayoutEncoder;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import lombok.extern.slf4j.Slf4j;

/**
 * @author changbo Created on 2020-04-23
 */
@Slf4j
public class FileErrorRollAppenderLoader extends BaseAppenderLoader implements ILogAppenderLoader {

	private static String appenderName = "FILE_ERROR_ROLL_LOG_APPENDER";

	@Override
	public void load(LoggerContext loggerContext, Environment environment) {
		Logger root = loggerContext.getLogger("ROOT");
		Appender<ILoggingEvent> logPlatform = root.getAppender(appenderName);
		if (logPlatform == null) {
			String errorLog = BASE_LOG_PATH + loggerName + "/error/error.log";
			String fileNamePattern = BASE_LOG_PATH + loggerName + "/error/error.%d{yyyy-MM-dd}.%i.log";

			RollingFileAppender logAppender = new RollingFileAppender();
			logAppender.setName(appenderName);
			logAppender.setFile(errorLog);
			logAppender.setContext(loggerContext);
			logAppender.setImmediateFlush(false);

			ThresholdFilter levelFilter = getLevelFilter(loggerContext);
			logAppender.addFilter(levelFilter);

			SizeAndTimeBasedRollingPolicy sizeAndTimeBasedRollingPolicy = getSizeAndTimeBasedRollingPolicy(
					loggerContext, fileNamePattern, logAppender);
			logAppender.setRollingPolicy(sizeAndTimeBasedRollingPolicy);

			FileConsolePatternLayoutEncoder encoder = getConsoleEncoder(loggerContext, environment);
			logAppender.setEncoder(encoder);
			root.addAppender(logAppender);
			logAppender.start();
		}
	}

	private ThresholdFilter getLevelFilter(LoggerContext loggerContext) {
		ThresholdFilter levelFilter = new ThresholdFilter();
		levelFilter.setLevel("ERROR");
		levelFilter.setName("errorFilter");
		levelFilter.setContext(loggerContext);
		levelFilter.start();
		return levelFilter;
	}
}
