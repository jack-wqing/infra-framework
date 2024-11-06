package com.jindi.infra.logger.encoder;

import static com.jindi.infra.logger.constant.LoggerConstants.*;
import static com.jindi.infra.logger.dto.LogInfo.buildLogInfo;

import java.nio.charset.Charset;

import com.jindi.infra.logger.logger.TycLogger;
import org.springframework.core.env.Environment;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.logger.dto.LogInfo;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author changbo Created on 2019-09-20
 */
public class FileMainPatternLayoutEncoder extends PatternLayoutEncoder {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public FileMainPatternLayoutEncoder(Environment environment) {
		String showLineNumber = environment.getProperty(mainShowLineNumberKey);
		if ("true".equals(showLineNumber)) {
			setPattern(fileLinePattern);
		} else {
			setPattern(fileNoLinePattern);
		}
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public byte[] encode(ILoggingEvent event) {
		String log = layout.doLayout(event);
		LogInfo logInfo = buildLogInfo(log, event.getMDCPropertyMap().get(TycLogger.LOG_DATA_TAG));
		return (InnerJSONUtils.toJSONString(logInfo) + LINE_SEPARATOR).getBytes(Charset.defaultCharset());
	}
}
