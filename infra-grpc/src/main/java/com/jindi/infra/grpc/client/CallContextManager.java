package com.jindi.infra.grpc.client;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.core.annotation.RPCCallOption;
import com.jindi.infra.core.annotation.RPCFallback;
import com.jindi.infra.grpc.RpcProperties;
import com.jindi.infra.grpc.model.CallContext;
import com.jindi.infra.grpc.util.ACUtils;
import com.jindi.infra.grpc.util.MethodUtils;

import io.grpc.BindableService;
import lombok.extern.slf4j.Slf4j;

/**
 * 调用远程方法的上下文信息管理
 */
@Slf4j
public class CallContextManager {

	private static final int DEFAULT_CALL_TIMEOUT_MILLIS = 1000;
	private static final int ZERO_INT = 0;
	private final Map<Method, CallContext> methodCallContextMap = new ConcurrentHashMap<>();
	@Autowired
	private RpcProperties rpcProperties;

	/**
	 * @param method
	 * @return
	 */
	public CallContext getCallContext(Method method) {
		return methodCallContextMap.get(method);
	}

	/**
	 * 注册服务
	 *
	 * @param serviceClass
	 * @throws NoSuchMethodException
	 */
	public void registerService(Class<BindableService> serviceClass, String serverName) throws Exception {
		Method[] methods = serviceClass.getDeclaredMethods();
		for (Method method : methods) {
			if (!MethodUtils.isAsyncMethod(method) && MethodUtils.isRemoteMethod(method)) {
				registerMethod(serviceClass, method, serverName);
			}
		}
	}

	/**
	 * 注册方法
	 *
	 * @param serviceClass
	 * @param method
	 * @throws NoSuchMethodException
	 */
	public void registerMethod(Class<BindableService> serviceClass, Method method, String serverName)
			throws NoSuchMethodException {
		CallContext callContext = new CallContext();
		callContext.setServiceClass(serviceClass);
		callContext.setMethod(method);
		callContext.setServerName(serverName);
		List beans = ACUtils.getBeansOfType(serviceClass);
		if (!CollectionUtils.isEmpty(beans)) {
			for (Object fallback : beans) {
				if (fallback != null && fallback.getClass().isAnnotationPresent(RPCFallback.class)) {
					callContext.setFallback(fallback);
					RPCCallOption rpcCallOption = fallback.getClass()
							.getDeclaredMethod(method.getName(), method.getParameterTypes())
							.getAnnotation(RPCCallOption.class);
					if (rpcCallOption != null) {
						callContext.setException(rpcCallOption.exception());
					}
					break;
				}
			}
		}
		methodCallContextMap.put(method, callContext);
	}

	/**
	 * 获取调用超时
	 */
	public Integer getCallTimeoutMillis(CallContext callContext, int serviceCallTimeoutMillis) {
		int callTimeoutMillis = serviceCallTimeoutMillis;
		if (CollectionUtils.isEmpty(rpcProperties.getClient().getServices())) {
			return callTimeoutMillis;
		}
		for (ClientProperties.ServiceConfig serviceConfig : rpcProperties.getClient().getServices()) {
			if (!Objects.equals(serviceConfig.getName(), callContext.getServerName())) {
				continue;
			}
			if (serviceConfig.getCallTimeoutMillis() > ZERO_INT) {
				callTimeoutMillis = serviceConfig.getCallTimeoutMillis();
			}
			if (CollectionUtils.isEmpty(serviceConfig.getMethods())) {
				break;
			}
			for (ClientProperties.MethodConfig methodConfig : serviceConfig.getMethods()) {
				String methodName = String.format("%s.%s", callContext.getServiceClass().getName(),
						callContext.getMethod().getName());
				if (!Objects.equals(methodConfig.getName(), methodName)) {
					continue;
				}
				if (methodConfig.getCallTimeoutMillis() > ZERO_INT) {
					callTimeoutMillis = methodConfig.getCallTimeoutMillis();
				}
				break;
			}
			break;
		}
		return callTimeoutMillis;
	}

	/**
	 * 获取方法的配置属性
	 *
	 * @param callContext
	 * @return
	 */
	public ClientProperties.MethodConfig getMethodConfig(CallContext callContext) {
		List<ClientProperties.ServiceConfig> serviceConfigs = rpcProperties.getClient().getServices();
		if (CollectionUtils.isEmpty(serviceConfigs)) {
			return null;
		}
		for (ClientProperties.ServiceConfig serviceConfig : serviceConfigs) {
			if (!Objects.equals(serviceConfig.getName(), callContext.getServerName())) {
				continue;
			}
			List<ClientProperties.MethodConfig> methodConfigs = serviceConfig.getMethods();
			if (CollectionUtils.isEmpty(methodConfigs)) {
				break;
			}
			for (ClientProperties.MethodConfig methodConfig : methodConfigs) {
				String methodName = String.format("%s.%s", callContext.getServiceClass().getName(),
						callContext.getMethod().getName());
				if (Objects.equals(methodConfig.getName(), methodName)) {
					return methodConfig;
				}
			}
			break;
		}
		return null;
	}
}
