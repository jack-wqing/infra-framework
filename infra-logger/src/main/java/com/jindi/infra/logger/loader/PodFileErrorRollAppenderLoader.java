package com.jindi.infra.logger.loader;

import static com.jindi.infra.common.constant.LoggerPath.BASE_LOG_PATH;
import static com.jindi.infra.logger.constant.LoggerConstants.*;

import org.springframework.core.env.Environment;

import com.jindi.infra.common.loader.ILogAppenderLoader;
import com.jindi.infra.logger.encoder.FileConsolePatternLayoutEncoder;

import ch.qos.logback.classic.AsyncAppender;
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
public class PodFileErrorRollAppenderLoader extends BaseAppenderLoader implements ILogAppenderLoader {

	private static String appenderName = "POD_FILE_ERROR_ROLL_LOG_APPENDER";
	private static String asyncAppenderName = "POD_FILE_ERROR_ROLL_LOG_APPENDER_ASYNC";

	private String errorLog = BASE_LOG_PATH + loggerName + "/error/" + podName + "/error.log";
	private String fileNamePattern = BASE_LOG_PATH + loggerName + "/error/" + podName + "/error.%d{yyyy-MM-dd}.%i.log";

	public static ThreadLocal<AsyncAppender> podFileErrorAsyncAppender = new ThreadLocal<>();

	@Override
	public void load(LoggerContext loggerContext, Environment environment) {
		Logger root = loggerContext.getLogger("ROOT");
		Appender<ILoggingEvent> logPlatform = root.getAppender(appenderName);
		if (logPlatform == null) {
			RollingFileAppender logAppender = getRollingFileAppender(loggerContext, environment);
			AsyncAppender asyncAppender = getAsyncAppender(loggerContext, logAppender, environment);
			root.addAppender(asyncAppender);
		}
	}

	private AsyncAppender getAsyncAppender(LoggerContext loggerContext, RollingFileAppender logAppender, Environment environment) {
		AsyncAppender asyncAppender = new AsyncAppender();
		asyncAppender.setContext(loggerContext);
		asyncAppender.setName(asyncAppenderName);
		asyncAppender.addAppender(logAppender);
		asyncAppender.setQueueSize(Integer.parseInt(environment.getProperty(asyncQueueSize, asyncDefaultValue))); // 设置队列大小
		asyncAppender.setMaxFlushTime(Integer.parseInt(environment.getProperty(asyncFlushTime, asyncDefaultValue))); // 设置最大刷盘时间
		asyncAppender.setNeverBlock(true); // 设置为非阻塞
		asyncAppender.start();

		// 这里还没有初始化applicationContext，使用ThreadLocal存储
		podFileErrorAsyncAppender.set(asyncAppender);
		return asyncAppender;
	}

	private RollingFileAppender getRollingFileAppender(LoggerContext loggerContext, Environment environment) {
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
		logAppender.start();
		return logAppender;
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
