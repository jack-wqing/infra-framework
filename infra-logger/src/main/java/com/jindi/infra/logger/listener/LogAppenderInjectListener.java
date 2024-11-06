package com.jindi.infra.logger.listener;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.ServiceLoader;

import ch.qos.logback.classic.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.loader.ILogAppenderLoader;
import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.logger.constant.LoggerConstants;
import com.jindi.infra.logger.converter.*;
import com.jindi.infra.logger.loggerFactory.TycLoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import lombok.extern.slf4j.Slf4j;

import static com.jindi.infra.common.util.InnerEnvironmentUtils.isProd;
import static com.jindi.infra.common.util.InnerEnvironmentUtils.isYufa;
import static com.jindi.infra.logger.constant.LoggerConstants.*;

/**
 * 此Listener在LoggingApplicationListener之后执行，可以保证配置文件的加载优先于此Listener，防止出现同一个name的Appender被添加两次
 * 具体参考SpringApplication#prepareEnvironment和LoggingApplicationListener#environmentPrepared
 *
 * @author changbo Created on 2020-04-13
 */
@Slf4j
public class LogAppenderInjectListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	private Environment environment;

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		environment = event.getEnvironment();
		LoggerContext loggerContext = TycLoggerFactory.getLoggerContext();
		init(loggerContext);
		// SPI的注入方式
		ServiceLoader<ILogAppenderLoader> serviceLoader = ServiceLoader.load(ILogAppenderLoader.class);
		Iterator<ILogAppenderLoader> it = serviceLoader.iterator();
		while (it.hasNext()) {
			ILogAppenderLoader loader = it.next();
			loader.load(loggerContext, environment);
		}
		// 预发和线上不再输出console，只保留console file
		if(!showConsole() && (isYufa(environment.getActiveProfiles()) || isProd(environment.getActiveProfiles()))) {
			Logger root = loggerContext.getLogger(ROOT);
			root.detachAppender(CONSOLE);
		}
	}

	private void init(LoggerContext loggerContext) {
		initApplicationName();
		initLoggerName();
		initProfile();
		initLoggerConvert();
		initLoggerLevel(loggerContext);
		initPort();
		initPid();
		initIpAndHost();
		initFolder();
		initVersion();
		initPodName();
	}

	private void initPodName() {
		String podName = System.getenv("POD_NAME");
		if (StringUtils.isBlank(podName)) {
			podName = "podName";
		}
		LoggerConstants.podName = podName;
	}

	private void initVersion() {
		LoggerConstants.version = "1";
	}

	private void initFolder() {
		LoggerConstants.folder = System.getenv("POD_NAME");
		if (LoggerConstants.folder == null) {
			LoggerConstants.folder = System.currentTimeMillis() + "";
		}
	}

	private void initIpAndHost() {
		LoggerConstants.hostname = InnerIpUtils.getHostName();
		LoggerConstants.ip = InnerIpUtils.getIP();
	}

	private void initPid() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		LoggerConstants.pid = runtimeMXBean.getName().split("@")[0];
	}

	private void initPort() {
		LoggerConstants.webPort = environment.getProperty(CommonConstant.SERVER_PORT);
		LoggerConstants.rpcPort = environment.getProperty(CommonConstant.RPC_SERVER_PORT,
				CommonConstant.DEFAULT_RPC_SERVER_PORT);
	}

	private void initLoggerLevel(LoggerContext loggerContext) {
		loggerContext.getLogger("org.apache.catalina.startup.DigesterFactory").setLevel(Level.ERROR);
		loggerContext.getLogger("org.apache.catalina.util.LifecycleBase").setLevel(Level.ERROR);
		loggerContext.getLogger("org.apache.coyote.http11.Http11NioProtocol").setLevel(Level.WARN);
		loggerContext.getLogger("org.apache.sshd.common.util.SecurityUtils").setLevel(Level.WARN);
		loggerContext.getLogger("org.apache.tomcat.util.net.NioSelectorPool").setLevel(Level.WARN);
		loggerContext.getLogger("org.eclipse.jetty.util.component.AbstractLifeCycle").setLevel(Level.ERROR);
		loggerContext.getLogger("org.hibernate.validator.internal.util.Version").setLevel(Level.WARN);
		loggerContext.getLogger("org.springframework.boot.actuate.endpoint.jmx").setLevel(Level.WARN);
	}

	private void initLoggerConvert() {
		PatternLayout.defaultConverterMap.put("version", VersionConverter.class.getName());
		PatternLayout.defaultConverterMap.put("applicationName", ApplicationNameConverter.class.getName());
		PatternLayout.defaultConverterMap.put("profile", ProfileConverter.class.getName());
		PatternLayout.defaultConverterMap.put("ip", IpConverter.class.getName());
		PatternLayout.defaultConverterMap.put("hostname", HostnameConverter.class.getName());
		PatternLayout.defaultConverterMap.put("webPort", WebPortConverter.class.getName());
		PatternLayout.defaultConverterMap.put("rpcPort", RpcPortConverter.class.getName());
		PatternLayout.defaultConverterMap.put("pid", PidConverter.class.getName());
		PatternLayout.defaultConverterMap.put("line", LineConverter.class.getName());
		PatternLayout.defaultConverterMap.put("clr", ColorConverter.class.getName());
		PatternLayout.defaultConverterMap.put("wex", WhitespaceThrowableProxyConverter.class.getName());
		PatternLayout.defaultConverterMap.put("wEx", ExtendedWhitespaceThrowableProxyConverter.class.getName());
	}

	private void initProfile() {
		LoggerConstants.profile = environment.getProperty(CommonConstant.PROFILE);
	}

	private void initApplicationName() {
		LoggerConstants.applicationName = environment.getProperty(CommonConstant.APPLICATION_NAME);
	}

	private void initLoggerName() {
		LoggerConstants.loggerName = environment.getProperty(CommonConstant.LOGGER_NAME);
		if (StringUtils.isBlank(LoggerConstants.loggerName)) {
			LoggerConstants.loggerName = environment.getProperty(CommonConstant.APPLICATION_NAME);
		}
	}

	private boolean showConsole() {
		String showConsole = environment.getProperty(consoleShowKey);
		if ("true".equals(showConsole)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 30;
	}
}
