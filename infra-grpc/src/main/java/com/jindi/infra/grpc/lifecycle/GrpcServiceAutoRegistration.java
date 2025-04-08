package com.jindi.infra.grpc.lifecycle;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jindi.infra.grpc.warmup.WarmUpRunner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.grpc.client.GrpcClientProxy;
import com.jindi.infra.grpc.extension.DiscoveryProvider;
import com.jindi.infra.grpc.server.GrpcServiceProxy;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GrpcServiceAutoRegistration implements SmartLifecycle, Ordered, SmartApplicationListener, ApplicationContextAware {

	private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

	private final GrpcServiceProxy grpcServiceProxy;

	private final GrpcClientProxy grpcClientProxy;

	@Autowired(required = false)
	private List<DiscoveryProvider> discoveryProviders;

	@Autowired(required = false)
	private List<WarmUpRunner> warmUpRunners;

	private ApplicationContext context;

	public GrpcServiceAutoRegistration(GrpcServiceProxy grpcServiceProxy, GrpcClientProxy grpcClientProxy) {
		this.grpcClientProxy = grpcClientProxy;
		this.grpcServiceProxy = grpcServiceProxy;
	}

	@Override
	public void start() {
		try {
			if (!RUNNING.compareAndSet(false, true)) {
				return;
			}
			grpcServiceProxy.start();
			grpcClientProxy.start();
			warmUp();
			register();
		} catch (Throwable e) {
			log.error("启动失败！", e);
			System.exit(-1);
		}
	}

	private void warmUp() {
		if (!CollectionUtils.isEmpty(warmUpRunners)) {
			try {
				for (WarmUpRunner runner : warmUpRunners) {
					runner.warmUp();
					log.info("warmup {} finish", runner.getClass());
				}
			} catch (Exception e) {
				log.error("warmup error", e);
			}
		}
	}

	@Override
	public void stop() {
		try {
			if (!RUNNING.compareAndSet(true, false)) {
				return;
			}
			log.info("开始关闭RPC服务");
			if (!CollectionUtils.isEmpty(discoveryProviders)) {
				for (DiscoveryProvider discoveryProvider : discoveryProviders) {
					discoveryProvider.unregister();
				}
			}
			try {
				// 消费者nacos客户端本地缓存更新，存在1秒左右的延迟
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				log.info("interrupted signal");
			}
			// shutdown阶段后移，在reboot里完成
//			grpcServiceProxy.shutdown();
//			grpcClientProxy.shutdown();
			log.info("关闭RPC服务结束");
		} catch (Throwable e) {
			log.error("GrpcServiceAutoRegistration 关闭失败", e);
		}
	}

	private void register() throws Throwable {
		if (CollectionUtils.isEmpty(discoveryProviders)) {
			return;
		}
		for (DiscoveryProvider discoveryProvider : discoveryProviders) {
			discoveryProvider.register();
		}
	}

	@Override
	public boolean isRunning() {
		return this.RUNNING.get();
	}

	@Override
	public int getPhase() {
		return Ordered.LOWEST_PRECEDENCE - 2000;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextClosedEvent) {
			if (((ContextClosedEvent) event).getApplicationContext() == this.context) {
				stop();
			}
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ContextClosedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
