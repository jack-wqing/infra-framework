package com.jindi.infra.grpc.pure.prometheus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.grpc.pure.constant.MiddlewareConfig;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushGatewayInitializer {

	private static final String CLOUD = "cloud";
	private static final String APPLICATION = "application";
	private static final String ENV = "env";
	private static final String INFRA_GRPC_PURE = "infra-grpc-pure";
	private static ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
	private static AtomicBoolean running = new AtomicBoolean(false);

	public static void initPushGateway(String appName, MiddlewareConfig middlewareConfig) {
		if (!running.compareAndSet(false, true)) {
			return;
		}
		if (StringUtils.isBlank(middlewareConfig.getPrometheusGatewayAddress())) {
			return;
		}
		PushGateway pushGateway = new PushGateway(middlewareConfig.getPrometheusGatewayAddress());
		Map<String, String> groupingKey = new HashMap<>();
		groupingKey.put(CLOUD, middlewareConfig.getCloudPlatform().getName());
		groupingKey.put(APPLICATION, appName);
		groupingKey.put(ENV, middlewareConfig.getEnv().getName());
		scheduledService.scheduleAtFixedRate(() -> {
			try {
				pushGateway.pushAdd(CollectorRegistry.defaultRegistry, INFRA_GRPC_PURE, groupingKey);
			} catch (IOException e) {
				log.error("", e);
			}
		}, 0, 15, TimeUnit.SECONDS);
	}
}
