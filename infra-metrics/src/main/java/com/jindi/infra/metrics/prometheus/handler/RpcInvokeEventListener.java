package com.jindi.infra.metrics.prometheus.handler;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationListener;

import com.jindi.infra.core.model.RpcInvokeEvent;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class RpcInvokeEventListener implements ApplicationListener<RpcInvokeEvent> {

	private static final Counter.Builder rpcClientTotalBuilder = Counter.build().namespace("rpc").subsystem("client")
			.name("total").labelNames("service", "method", "methodType", "eventType").help("rpc 客户端调用事件计数");

	private Counter rpcClientTotalCounter;
	private CollectorRegistry collectorRegistry;

	public RpcInvokeEventListener(CollectorRegistry collectorRegistry) {
		this.collectorRegistry = collectorRegistry;
	}

	@PostConstruct
	public void initMethod() {
		rpcClientTotalCounter = rpcClientTotalBuilder.register(collectorRegistry);
	}

	@Override
	public void onApplicationEvent(RpcInvokeEvent event) {
		rpcClientTotalCounter.labels(event.getService(), event.getMethod(), event.getMethodType().name(),
				event.getEventType().name()).inc();
	}
}
