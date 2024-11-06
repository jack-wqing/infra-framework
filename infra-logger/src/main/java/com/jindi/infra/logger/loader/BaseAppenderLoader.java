package com.jindi.infra.logger.loader;

import static com.jindi.infra.logger.constant.LoggerConstants.*;

import org.springframework.core.env.Environment;

import com.jindi.infra.logger.encoder.FileConsolePatternLayoutEncoder;
import com.jindi.infra.logger.encoder.FileMainPatternLayoutEncoder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

public class BaseAppenderLoader {

	protected FileMainPatternLayoutEncoder getMainEncoder(LoggerContext loggerContext, Environment environment) {
		FileMainPatternLayoutEncoder encoder = new FileMainPatternLayoutEncoder(environment);
		encoder.setContext(loggerContext);
		encoder.start();
		return encoder;
	}

	protected FileConsolePatternLayoutEncoder getConsoleEncoder(LoggerContext loggerContext, Environment environment) {
		FileConsolePatternLayoutEncoder encoder = new FileConsolePatternLayoutEncoder(environment);
		encoder.setContext(loggerContext);
		encoder.start();
		return encoder;
	}

	protected SizeAndTimeBasedRollingPolicy getSizeAndTimeBasedRollingPolicy(LoggerContext loggerContext,
			String fileNamePattern, RollingFileAppender logAppender) {
		SizeAndTimeBasedRollingPolicy sizeAndTimeBasedRollingPolicy = new SizeAndTimeBasedRollingPolicy();
		sizeAndTimeBasedRollingPolicy.setContext(loggerContext);
		sizeAndTimeBasedRollingPolicy.setFileNamePattern(fileNamePattern);
		sizeAndTimeBasedRollingPolicy.setMaxFileSize(FileSize.valueOf(fileSize));
		sizeAndTimeBasedRollingPolicy.setMaxHistory(maxHistoryDays);
		sizeAndTimeBasedRollingPolicy.setTotalSizeCap(FileSize.valueOf(totalSize));
		sizeAndTimeBasedRollingPolicy.setParent(logAppender);
		sizeAndTimeBasedRollingPolicy.start();
		return sizeAndTimeBasedRollingPolicy;
	}
}
