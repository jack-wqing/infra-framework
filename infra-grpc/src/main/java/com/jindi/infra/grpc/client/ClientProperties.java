package com.jindi.infra.grpc.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.jindi.infra.grpc.util.ExecutorsUtils;
import lombok.Data;

/**
 * 客户端配置
 */
@Data
public class ClientProperties {

	/**
	 * 连接超时时长
	 */
	private Integer connectTimeoutMillis = 1000;

	/**
	 * 调用超时
	 */
	private Integer callTimeoutMillis = 1000;

	/**
	 * ping 调用超时
	 */
	private Integer pingCallTimeoutMillis = 3000;

	/**
	 * 工作线程数
	 */
	private Integer workerThreads = (int) Short.MAX_VALUE;

	/**
	 * 线程池类型
	 */
	private String threadPool = ExecutorsUtils.CACHED;

	/**
	 * 服务配置
	 */
	private List<ServiceConfig> services = new ArrayList<>();

	private List<Server> servers = new ArrayList<>();
	private AtomicReference<Map<String, Server>> serverNameServer = new AtomicReference<>();

	public Map<String, Server> getServerNameServer() {
		if (serverNameServer.get() != null) {
			return serverNameServer.get();
		}
		serverNameServer
				.set(servers.stream().collect(Collectors.toMap(server -> server.getServerName(), server -> server)));
		return serverNameServer.get();
	}

	@Data
	public static class ServiceConfig {

		/**
		 * 项目名
		 */
		private String name;

		/**
		 * 调用超时
		 */
		private Integer callTimeoutMillis = -1;

		/**
		 * 方法配置
		 */
		private List<MethodConfig> methods = new ArrayList<>();
	}

	@Data
	public static class MethodConfig {

		/**
		 * 服务名.方法名
		 */
		private String name;

		/**
		 * 调用超时
		 */
		private Integer callTimeoutMillis = -1;

		/**
		 * 重试次数
		 */
		private Integer retryCount = 0;

		/**
		 * 服务手动降级
		 */
		private Boolean manualDegrade = false;
	}

	@Data
	public static class Server {

		/**
		 * 服务名
		 */
		private String serverName;

		/**
		 * 客户端直连
		 */
		private String target = "localhost:9999";

		/**
		 * 直连
		 */
		private Boolean direct = false;
	}
}
