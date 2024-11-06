package com.jindi.infra.logger.dto;

import lombok.Data;

/**
 * 监控参数
 *
 * @author changbo Created on 2019-10-13
 */
@Data
public class Monitor {

	private String projectName;
	// 当前时间
	private String time;
	// 线程池参数
	private int corePoolSize;
	private int maxPoolSize;
	private int aliveTime;
	private String timeUnit;
	// 阻塞队列容量
	private int capacity;
	// 活跃数量
	private int poolSize;
	// 运行中数量
	private int activeCount;
	// 阻塞队列当前值
	private int queueSize;
	// 异常数
	private int exceptionCount;
	// 当前接入应用使用的版本
	private int version;
}
