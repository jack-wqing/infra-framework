package com.jindi.infra.logger.constant;

import lombok.extern.slf4j.Slf4j;

/**
 * @author changbo
 * @date 2021/7/14
 */
@Slf4j
public class LoggerConstants {

	public static final String fileLinePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS}|%pid|%X{traceId:-}|%X{parentId:-}|%X{spanId:-}|%X{sampled:-}|%X{userId:-}|%ip|%hostname|%webPort|%rpcPort|%applicationName|%profile|%version|%line|%thread|%level|%logger|%msg%n";
	public static final String fileNoLinePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS}|%pid|%X{traceId:-}|%X{parentId:-}|%X{spanId:-}|%X{sampled:-}|%X{userId:-}|%ip|%hostname|%webPort|%rpcPort|%applicationName|%profile|%version|-1|%thread|%level|%logger|%msg%n";
	// public static final String fileNoLinePattern = "%d{yyyy-MM-dd
	// HH:mm:ss.SSS}|%pid|%X{traceId:-}|%X{parentId:-}|%X{spanId:-}|%X{extra:-}|%X{logType:-}|%X{userId:-}|%X{index:-}|-1|%thread|%level|%logger|%msg%n";
	// public static final String fileLinePattern = "{\"time\":\"%d{yyyy-MM-dd
	// HH:mm:ss.SSS}\",\"pid\":\"%pid\",\"traceId\":\"%X{traceId:-}\",\"parentId\":\"%X{parentId:-}\",\"spanId\":\"%X{spanId:-}\",\"ip\":\"%ip\",\"hostname\":\"%hostname\",\"webPort\":\"%webPort\",\"rpcPort\":\"%rpcPort\",\"extra\":\"%X{extra:-}\",\"logType\":\"%X{logType:-}\",\"
	// userId\":\"%X{userId:-}\",\"customizeIndex\":\"%X{index:-}\",\"lineNum\":\"%line\",
	// \"threadName\":\"%thread\",\"level\":\"%level\",\"clazz\":\"%logger\",\"version\":\"%version\",\"applicationName\":\"%applicationName\",\"profile\":\"%profile\",\"log\":\"%msg\"}\n";
	// public static final String fileNoLinePattern = "{\"time\":\"%d{yyyy-MM-dd
	// HH:mm:ss.SSS}\",\"pid\":\"%pid\",\"traceId\":\"%X{traceId:-}\",\"parentId\":\"%X{parentId:-}\",\"spanId\":\"%X{spanId:-}\",\"ip\":\"%ip\",\"hostname\":\"%hostname\",\"webPort\":\"%webPort\",\"rpcPort\":\"%rpcPort\",\"extra\":\"%X{extra:-}\",\"logType\":\"%X{logType:-}\",\"
	// userId\":\"%X{userId:-}\",\"customizeIndex\":\"%X{index:-}\",\"lineNum\":\"-1\",
	// \"threadName\":\"%thread\",\"level\":\"%level\",\"clazz\":\"%logger\",\"version\":\"%version\",\"applicationName\":\"%applicationName\",\"profile\":\"%profile\",\"log\":\"%msg\"}\n";
	public static final String consoleLinePattern = "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p [%X{traceId:-},%X{parentId:-},%X{spanId:-},%X{sampled:-}]) %clr(%pid){magenta} %clr([%15.15t]){faint} %clr(---){faint} %clr(%-40.40logger{39}){cyan} %clr([%line]) [%X{logdata:-}]: %m%n%wEx";
	public static final String consoleNoLinePattern = "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p [%X{traceId:-},%X{parentId:-},%X{spanId:-},%X{sampled:-}]) %clr(%pid){magenta} %clr([%15.15t]){faint} %clr(---){faint} %clr(%-40.40logger{39}){cyan}[%X{logdata:-}]: %m%n%wEx";

	public static final String fileConsoleLinePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %p [%ip,%X{traceId:-},%X{parentId:-},%X{spanId:-},%X{sampled:-}] %pid [%thread] --- %-40.40logger{39} [%line] [|] %X{logdata:-} [|] %m%n%wEx";
	public static final String fileConsoleNoLinePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %p [%ip,%X{traceId:-},%X{parentId:-},%X{spanId:-},%X{sampled:-}] %pid [%thread] --- %-40.40logger{39} [|] %X{logdata:-} [|] %m%n%wEx";

	/**
	 * 日志配置
	 */
	public static String consoleShowLineNumberKey = "logger.console.showLineNumber";
	public static String mainShowLineNumberKey = "logger.main.showLineNumber";
	public static String consoleShowKey = "logger.console.out";
	public static String mainShowKey = "logger.main.out";
	/**
	 * 异步日志配置
	 */
	public static String asyncQueueSize = "logger.async.queueSize";
	public static String asyncFlushTime = "logger.async.flushTime";
	public static String asyncDefaultValue = "1000";

	public static String fileSize = "1GB";
	public static String totalSize = "5GB";
	public static int maxHistoryDays = 5;

	public static final String ROOT = "ROOT";
	public static final String CONSOLE = "CONSOLE";

	/**
	 * 应用名
	 */
	public static String applicationName;
	/**
	 * 日志名
	 */
	public static String loggerName;
	/**
	 * 进程号
	 */
	public static String pid;
	/**
	 * 启动环境变量
	 */
	public static String profile;
	/**
	 * 日志目录名
	 */
	public static String folder;

	public static String ip;

	public static String hostname;

	public static String webPort;

	public static String rpcPort;

	public static String version;

	public static String podName;
}
