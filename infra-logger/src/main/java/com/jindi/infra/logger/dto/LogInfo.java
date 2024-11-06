package com.jindi.infra.logger.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Log信息
 *
 * @author changbo Created on 2019-10-16
 */
@Data
public class LogInfo {

	private long id;
	private String time;
	private String pid;
	private String traceId;
	private String parentId;
	private String spanId;
	private String sampled;
	private String userId;
	private String ip;
	private String hostname;
	private String webPort;
	private String rpcPort;
	private String applicationName;
	private String profile;
	private String version;
	private String lineNum;
	private String threadName;
	private String level;
	private String clazz;
	private String log;
	private String logdata;

	public static LogInfo buildLogInfo(String log) {
		return buildLogInfo(log, null);
	}
	public static LogInfo buildLogInfo(String log, String logdata) {
		if (StringUtils.isBlank(log)) {
			return null;
		}
		String[] split = log.split("\\|");
		LogInfo logInfo = new LogInfo();
		logInfo.setId(System.nanoTime());
		logInfo.setTime(split[0]);
		logInfo.setPid(split[1]);
		logInfo.setTraceId(split[2]);
		logInfo.setParentId(split[3]);
		logInfo.setSpanId(split[4]);
		logInfo.setSampled(split[5]);
		logInfo.setUserId(split[6]);
		logInfo.setIp(split[7]);
		logInfo.setHostname(split[8]);
		logInfo.setWebPort(split[9]);
		logInfo.setRpcPort(split[10]);
		logInfo.setApplicationName(split[11]);
		logInfo.setProfile(split[12]);
		logInfo.setVersion(split[13]);
		logInfo.setLineNum(split[14]);
		logInfo.setThreadName(split[15]);
		logInfo.setLevel(split[16]);
		logInfo.setClazz(split[17]);
		logInfo.setLog(split[18]);
		logInfo.setLogdata(logdata);
		return logInfo;
	}

	private static Map<String, String> toMap(String str) {
		if (StringUtils.isBlank(str)) {
			return new HashMap<>();
		}
		try {
			return JSON.parseObject(str, new TypeReference<Map<String, String>>() {
			});
		} catch (Exception e) {
			return new HashMap<>();
		}
	}
}
