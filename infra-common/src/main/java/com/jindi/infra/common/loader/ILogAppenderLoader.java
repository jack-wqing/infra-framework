package com.jindi.infra.common.loader;

import org.springframework.core.env.Environment;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author changbo Created on 2020-04-14
 */
public interface ILogAppenderLoader {

	void load(LoggerContext loggerContext, Environment env);
}
