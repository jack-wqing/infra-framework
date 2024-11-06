package com.jindi.infra.trace.reporter;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.jindi.infra.common.constant.LoggerPath;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.trace.model.Span;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileSpanReporter {

	private static final String ZIPKIN = "zipkin";
	private static final String MAX_FILE_SIZE = "1GB";
	private static final String TOTAL_SIZE_CAP = "5GB";
	private static final int MAX_HISTORY = 2;
	private static final String PATTERN = "%m%n";
	private Logger zipkinWriter;
	private String podName;

	public FileSpanReporter(String loggerPath) {
		podName = getPodName();
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		zipkinWriter = loggerContext.getLogger(ZIPKIN);
		zipkinWriter.setAdditive(false);
		RollingFileAppender podRollingFileAppender = getPodRollingFileAppender(loggerContext, loggerPath);
		zipkinWriter.addAppender(podRollingFileAppender);
	}

	private RollingFileAppender getPodRollingFileAppender(LoggerContext loggerContext, String loggerPath) {
		RollingFileAppender rollingFileAppender = new RollingFileAppender();
		rollingFileAppender.setName(ZIPKIN);
		rollingFileAppender.setImmediateFlush(false);
		rollingFileAppender.setFile(LoggerPath.BASE_LOG_PATH + loggerPath + "/zipkin/" + podName + "/new-zipkin.log");
		rollingFileAppender.setContext(loggerContext);
		SizeAndTimeBasedRollingPolicy sizeAndTimeBasedRollingPolicy = getSizeAndTimeBasedRollingPolicy(loggerContext,
				LoggerPath.BASE_LOG_PATH + loggerPath + "/zipkin/" + podName + "/new-zipkin-%d{yyyy-MM-dd}.%i.log", rollingFileAppender);
		rollingFileAppender.setRollingPolicy(sizeAndTimeBasedRollingPolicy);
		PatternLayoutEncoder encoder = getPatternLayoutEncoder(loggerContext);
		rollingFileAppender.setEncoder(encoder);
		rollingFileAppender.start();
		return rollingFileAppender;
	}

	private PatternLayoutEncoder getPatternLayoutEncoder(LoggerContext loggerContext) {
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern(PATTERN);
		encoder.setCharset(StandardCharsets.UTF_8);
		encoder.setContext(loggerContext);
		encoder.start();
		return encoder;
	}

	private SizeAndTimeBasedRollingPolicy getSizeAndTimeBasedRollingPolicy(LoggerContext loggerContext,
			String fileNamePattern, RollingFileAppender rollingFileAppender) {
		SizeAndTimeBasedRollingPolicy sizeAndTimeBasedRollingPolicy = new SizeAndTimeBasedRollingPolicy();
		sizeAndTimeBasedRollingPolicy.setContext(loggerContext);
		sizeAndTimeBasedRollingPolicy.setFileNamePattern(fileNamePattern);
		sizeAndTimeBasedRollingPolicy.setMaxFileSize(FileSize.valueOf(MAX_FILE_SIZE));
		sizeAndTimeBasedRollingPolicy.setMaxHistory(MAX_HISTORY);
		sizeAndTimeBasedRollingPolicy.setTotalSizeCap(FileSize.valueOf(TOTAL_SIZE_CAP));
		sizeAndTimeBasedRollingPolicy.setParent(rollingFileAppender);
		sizeAndTimeBasedRollingPolicy.start();
		return sizeAndTimeBasedRollingPolicy;
	}

	public void report(Span span) {
		if (span == null) {
			return;
		}
		zipkinWriter.info(InnerJSONUtils.toJSONString(span));
	}

	private String getPodName() {
		String podName = System.getenv("POD_NAME");
		if (StringUtils.isBlank(podName)) {
			podName = "podName";
		}
		return podName;
	}
}
