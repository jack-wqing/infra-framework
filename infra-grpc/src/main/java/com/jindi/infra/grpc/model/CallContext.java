package com.jindi.infra.grpc.model;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.grpc.extension.Node;
import io.grpc.BindableService;

import java.lang.reflect.Method;

// 方法上下文记录
public class CallContext {

	private static final ThreadLocal<CallContext> CURRENT_CALL_CONTEXT = new ThreadLocal<>();

	/**
	 * 应用
	 */
	private String serverName;

	/**
	 * 接口
	 */
	private Class<BindableService> serviceClass;

	/**
	 * 方法
	 */
	private Method method;

	/**
	 * 回退实现
	 */
	private Object fallback;

	/**
	 * 异常列表
	 */
	private Class<?>[] exception;

	/**
	 * 调用服务节点
	 */
	private Node node;

	/**
	 * 设置线程当前RPC调用的上下文
	 *
	 * @param callContext
	 */
	public static void setCurrentCallContext(CallContext callContext) {
		CURRENT_CALL_CONTEXT.set(callContext);
	}

	/**
	 * 清除线程当前RPC调用的上下文
	 */
	public static void removeCallContext() {
		CallContext callContext = currentCallContext();
		if (callContext != null) {
			callContext.setNode(null);
		}
		CURRENT_CALL_CONTEXT.remove();
	}

	/**
	 * 获取线程当前RPC调用的上下文
	 */
	public static CallContext currentCallContext() {
		return CURRENT_CALL_CONTEXT.get();
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Class<BindableService> getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(Class<BindableService> serviceClass) {
		this.serviceClass = serviceClass;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getFallback() {
		return fallback;
	}

	public void setFallback(Object fallback) {
		this.fallback = fallback;
	}

	public Class<?>[] getException() {
		return exception;
	}

	public void setException(Class<?>[] exception) {
		this.exception = exception;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return InnerJSONUtils.toJSONString(this);
	}
}
