package com.jindi.infra.grpc.server;

import com.jindi.infra.grpc.util.ExecutorsUtils;
import lombok.Data;

/**
 * 服务端配置
 */
@Data
public class ServerProperties {

	/**
	 * 服务监听端口
	 */
	private Integer port = 9999;

	/**
	 * 等待终止时间
	 */
	private Integer awaitTerminationSecond = 5;

	/**
	 * 工作线程数
	 */
	private Integer workerThreads = 200;

	/**
	 * 线程池类型
	 */
	private String threadPool = ExecutorsUtils.FIXED;
}
