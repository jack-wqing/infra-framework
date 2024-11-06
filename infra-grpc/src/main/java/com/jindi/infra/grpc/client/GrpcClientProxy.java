package com.jindi.infra.grpc.client;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.core.annotation.RPCInterface;
import com.jindi.infra.grpc.channel.ChannelManager;
import com.jindi.infra.grpc.util.SdkConfigUtils;

import io.grpc.BindableService;
import lombok.extern.slf4j.Slf4j;

/**
 * Grpc客户端代理
 *
 * @author changbo
 * @date 2021/7/8
 */
@Slf4j
public class GrpcClientProxy {

	private static final AtomicBoolean OPEN = new AtomicBoolean(false);
	private final Map<String, Map<Class<BindableService>, Object>> regionServiceClassProxies = Collections.synchronizedMap(new HashMap<>());
	@Resource
	private CallContextManager callContextManager;
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private ObjectProvider<List<ChannelManager>> channelManagerObjectProvider;
	@Autowired
	private ObjectProvider<CatGrpcClientHandler> catGrpcClientHandler;

	/**
	 * 获取服务类的代理对象
	 *
	 * @param serviceClass
	 *            服务类
	 */
	public <T> T proxy(Class<T> serviceClass, String region, int serviceCallTimeoutMillis) {
		Objects.requireNonNull(serviceClass);
		if (!BindableService.class.isAssignableFrom(serviceClass)) {
			log.error("服务类 {} 没有实现 {} 接口", serviceClass.getTypeName(), BindableService.class.getTypeName());
			return null;
		}
		if (!serviceClass.isAnnotationPresent(RPCInterface.class)) {
			log.error("服务类 {} 没有使用注解 @RPCInterface 标记", serviceClass.getTypeName());
			return null;
		}
		Map<Class<BindableService>, Object> serviceClassProxies = regionServiceClassProxies.get(region);
		if (serviceClassProxies == null) {
			serviceClassProxies = Collections.synchronizedMap(new HashMap<>());
			regionServiceClassProxies.put(region, serviceClassProxies);
		}
		if (serviceClassProxies.containsKey(serviceClass)) {
			return (T) serviceClassProxies.get(serviceClass);
		}
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(serviceClass);
		ChannelManager channelManager = getChannelManager(region);
		Objects.requireNonNull(channelManager);
		enhancer.setCallback(new GrpcClientInvocationHandler((Class<BindableService>) serviceClass, callContextManager, channelManager, serviceCallTimeoutMillis, applicationEventPublisher, catGrpcClientHandler.getIfAvailable()));
		Object proxy = enhancer.create();
		serviceClassProxies.put((Class<BindableService>) serviceClass, proxy);
		return (T) proxy;
	}

	private ChannelManager getChannelManager(String region) {
		List<ChannelManager> channelManagers = channelManagerObjectProvider.getIfAvailable();
		if (CollectionUtils.isEmpty(channelManagers)) {
			return null;
		}
		for (ChannelManager channelManager : channelManagers) {
			if (Objects.equals(channelManager.getRegion(), region)) {
				return channelManager;
			}
		}
		return null;
	}

	/**
	 * 启动服务
	 */
	public void start() throws Throwable {
		if (!OPEN.compareAndSet(false, true)) {
			log.warn("RPC 客户端已经被启动");
			return;
		}
		boolean empty = true;
		for (Map.Entry<String, Map<Class<BindableService>, Object>> entry : regionServiceClassProxies.entrySet()) {
			if (!CollectionUtils.isEmpty(entry.getValue())) {
				empty = false;
				break;
			}
		}
		if (empty) {
			log.info("没有调用外部RPC服务");
			return;
		}
		log.info("RPC 客户端正在被启动");
		for (Map.Entry<String, Map<Class<BindableService>, Object>> entry : regionServiceClassProxies.entrySet()) {
			ChannelManager channelManager = getChannelManager(entry.getKey());
			Objects.requireNonNull(channelManager);
			Map<Class<BindableService>, Object> serviceClassProxies = entry.getValue();
			if (serviceClassProxies == null || serviceClassProxies.isEmpty()) {
				continue;
			}
			for (Class<BindableService> serviceClass : serviceClassProxies.keySet()) {
				String serverName = SdkConfigUtils.parseServerName(serviceClass);
				callContextManager.registerService(serviceClass, serverName);
				channelManager.initChannel(serverName);
			}
		}
		log.info("RPC 客户端启动成功");
	}

	/**
	 * 停止服务
	 *
	 * @throws Throwable
	 */
	public void shutdown() throws Throwable {
		if (!OPEN.compareAndSet(true, false)) {
			return;
		}
		List<ChannelManager> channelManagers = channelManagerObjectProvider.getIfAvailable();
		if (CollectionUtils.isEmpty(channelManagers)) {
			return;
		}
		for (ChannelManager channelManager : channelManagers) {
			channelManager.close();
		}
	}

	/**
	 * 获取所有的RPC服务类
	 */
	public List<Class<BindableService>> getAllServiceClass() {
		List<Class<BindableService>> serviceClass = new ArrayList<>();
		for (Map.Entry<String, Map<Class<BindableService>, Object>> entry : regionServiceClassProxies.entrySet()) {
			Map<Class<BindableService>, Object> serviceClassProxies = entry.getValue();
			if (serviceClassProxies == null || serviceClassProxies.isEmpty()) {
				continue;
			}
			serviceClass.addAll(serviceClassProxies.keySet());
		}
		return serviceClass;
	}
}
