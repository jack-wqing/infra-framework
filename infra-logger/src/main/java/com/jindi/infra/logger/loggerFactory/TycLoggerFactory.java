package com.jindi.infra.logger.loggerFactory;

import com.jindi.infra.logger.logger.TycLogger;
import org.slf4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.LoggerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author changbo Created on 2020-04-14
 */
public class TycLoggerFactory {

	public static Map<String, TycLogger> loggerMap = new ConcurrentHashMap<>();

	public static LoggerContext getLoggerContext() {
		return (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
	}

	public static TycLogger getLogger(String name) {
		if (loggerMap.containsKey(name)) {
			return loggerMap.get(name);
		}
		Logger logger = getLoggerContext().getLogger(name);
		loggerMap.put(name, new TycLogger(logger));
		return loggerMap.get(name);
	}

	public static TycLogger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}
}
