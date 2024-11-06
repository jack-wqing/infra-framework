package com.jindi.infra.logger.loader;

import static com.jindi.infra.common.constant.LoggerPath.BASE_LOG_PATH;
import static com.jindi.infra.logger.constant.LoggerConstants.loggerName;
import static com.jindi.infra.logger.constant.LoggerConstants.podName;

import org.springframework.core.env.Environment;

import com.jindi.infra.common.loader.ILogAppenderLoader;
import com.jindi.infra.logger.encoder.FileMainPatternLayoutEncoder;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import lombok.extern.slf4j.Slf4j;

/**
 * 这个RequestRoll给网关使用，由网关内部开启，框架只保留代码，暂不启用
 */
@Slf4j
public class PodFileRequestRollAppenderLoader extends BaseAppenderLoader implements ILogAppenderLoader {

	private static String appenderName = "POD_FILE_REQUEST_ROLL_LOG_APPENDER";

	@Override
	public void load(LoggerContext loggerContext, Environment environment) {
		Logger root = loggerContext.getLogger("CLOUD_GATEWAY_REQUEST_LOG");
		Appender<ILoggingEvent> logPlatform = root.getAppender(appenderName);
		if (logPlatform == null) {
			String mainLog = BASE_LOG_PATH + loggerName + "/main/" + podName + "/request/request.log";
			String fileNamePattern = BASE_LOG_PATH + loggerName + "/main/" + podName + "/request/request.%d{yyyy-MM-dd}.%i.log";

			RollingFileAppender logAppender = new RollingFileAppender();
			logAppender.setName(appenderName);
			logAppender.setFile(mainLog);
			logAppender.setContext(loggerContext);
			logAppender.setImmediateFlush(false);

			SizeAndTimeBasedRollingPolicy sizeAndTimeBasedRollingPolicy = getSizeAndTimeBasedRollingPolicy(
					loggerContext, fileNamePattern, logAppender);
			logAppender.setRollingPolicy(sizeAndTimeBasedRollingPolicy);

			FileMainPatternLayoutEncoder encoder = getMainEncoder(loggerContext, environment);
			logAppender.setEncoder(encoder);

			root.addAppender(logAppender);
			root.setAdditive(false);
			logAppender.start();
		}
	}
}
