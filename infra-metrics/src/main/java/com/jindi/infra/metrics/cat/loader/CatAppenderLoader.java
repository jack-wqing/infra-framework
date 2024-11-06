package com.jindi.infra.metrics.cat.loader;

import org.springframework.core.env.Environment;

import com.jindi.infra.common.loader.ILogAppenderLoader;
import com.jindi.infra.metrics.cat.appender.CatLogbackAppender;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * @author changbo
 * @date 2021/7/18
 */
public class CatAppenderLoader implements ILogAppenderLoader {

	@Override
	public void load(LoggerContext loggerContext, Environment env) {
		Logger root = loggerContext.getLogger("ROOT");
		Appender<ILoggingEvent> logPlatform = root.getAppender("CatAppender");
		if (logPlatform == null) {
			CatLogbackAppender appender = new CatLogbackAppender();
			appender.setContext(loggerContext);
			appender.setName("CatAppender");
			root.addAppender(appender);
			appender.start();
		}
	}
}
