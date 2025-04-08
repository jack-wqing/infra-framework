package com.jindi.infra.grpc.client;

import com.jindi.infra.grpc.util.ExecutorsUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Data
public class ClientProperties {

	private Integer connectTimeoutMillis = 1000;

	private Integer callTimeoutMillis = 1000;

	private Integer pingCallTimeoutMillis = 3000;

	private Integer workerThreads = (int) Short.MAX_VALUE;

	private String threadPool = ExecutorsUtils.CACHED;

	private List<ServiceConfig> services = new ArrayList<>();

	private List<Server> servers = new ArrayList<>();

	private AtomicReference<Map<String, Server>> serverNameServer = new AtomicReference<>();

	public Map<String, Server> getServerNameServer() {
		if (serverNameServer.get() != null) {
			return serverNameServer.get();
		}
		serverNameServer.set(servers.stream().collect(Collectors.toMap(server -> server.getServerName(), server -> server)));
		return serverNameServer.get();
	}

	@Data
	public static class ServiceConfig {

		private String name;

		private Integer callTimeoutMillis = -1;

		private List<MethodConfig> methods = new ArrayList<>();
	}

	@Data
	public static class MethodConfig {

		private String name;

		private Integer callTimeoutMillis = -1;

		private Integer retryCount = 0;

		private Boolean manualDegrade = false;
	}

	@Data
	public static class Server {

		private String serverName;

		private String target = "localhost:9999";

		private Boolean direct = false;
	}
}
