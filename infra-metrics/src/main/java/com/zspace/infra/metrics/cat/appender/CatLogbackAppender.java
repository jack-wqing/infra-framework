package com.zspace.infra.metrics.cat.appender;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.dianping.cat.Cat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;

/**
 * @author changbo
 * @date 2021/7/18
 */
public class CatLogbackAppender extends AppenderBase<ILoggingEvent> {

	public static final String STATUS = "0";
	public static final char C = '\n';
	private static final String TYPE = "Logback";

	@Override
	protected void append(ILoggingEvent event) {
		try {
			boolean isTraceMode = Cat.getManager().isTraceMode();
			Level level = event.getLevel();
			if (level.isGreaterOrEqual(Level.ERROR)) {
				logError(event);
			} else if (isTraceMode) {
				logTrace(event);
			}
		} catch (Throwable ex) {
			throw new LogbackException(event.getFormattedMessage(), ex);
		}
	}

	private void logError(ILoggingEvent event) {
		ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
		if (info == null) {
			return;
		}
		Throwable exception = info.getThrowable();
		Object message = event.getFormattedMessage();
		if (message != null) {
			Cat.logError(String.valueOf(message), exception);
		} else {
			Cat.logError(exception);
		}
	}

	private void logTrace(ILoggingEvent event) {
		Object message = event.getFormattedMessage();
		StringWriter data = new StringWriter();
		if (message instanceof Throwable) {
			((Throwable) message).printStackTrace(new PrintWriter(data));
		} else {
			data.append(event.getFormattedMessage());
		}
		ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
		if (info != null && info.getThrowable() != null) {
			data.append(C);
			info.getThrowable().printStackTrace(new PrintWriter(data));
		}
		Cat.logTrace(TYPE, event.getLevel().levelStr, STATUS, data.toString());
	}
}
