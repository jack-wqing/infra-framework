package com.jindi.infra.grpc.client;

import com.dianping.cat.message.Transaction;
import com.google.common.util.concurrent.Futures;
import com.jindi.infra.core.constants.EventType;
import com.jindi.infra.core.constants.MethodType;
import com.jindi.infra.core.exception.RpcBlockException;
import com.jindi.infra.core.exception.RpcClientException;
import com.jindi.infra.core.model.FrameworkLogEvent;
import com.jindi.infra.core.model.RpcInvokeEvent;
import com.jindi.infra.grpc.channel.ChannelManager;
import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.model.CallContext;
import com.jindi.infra.grpc.util.GrpcUtils;
import com.jindi.infra.grpc.util.MethodUtils;
import com.jindi.infra.grpc.util.NameUtils;
import com.jindi.infra.tools.RpcLatencyUtils;
import com.jindi.infra.tools.enums.RpcLatencyPeriodEnum;
import io.grpc.BindableService;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.InternalClientCalls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * rpc客户端调用处理器
 */
@Slf4j
public class GrpcClientInvocationHandler implements MethodInterceptor {

	private static final String INTERNAL_STUB_TYPE_STRING = "internal-stub-type";
	private static final CallOptions.Key INTERNAL_STUB_TYPE = CallOptions.Key.create(INTERNAL_STUB_TYPE_STRING);
	private final CallContextManager callContextManager;
	private ChannelManager channelManager;
	private final int serviceCallTimeoutMillis;
	private final Class<BindableService> serviceClass;
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private CatGrpcClientHandler catGrpcClientHandler;

	/**
	 * 构造器
	 */
	public GrpcClientInvocationHandler(Class<BindableService> serviceClass, CallContextManager callContextManager,
			ChannelManager channelManager, 	int serviceCallTimeoutMillis, ApplicationEventPublisher applicationEventPublisher, CatGrpcClientHandler catGrpcClientHandler) {
		this.serviceClass = serviceClass;
		this.callContextManager = callContextManager;
		this.channelManager = channelManager;
		this.serviceCallTimeoutMillis = serviceCallTimeoutMillis;
		this.applicationEventPublisher = applicationEventPublisher;
		this.catGrpcClientHandler = catGrpcClientHandler;
	}

	/**
	 * 判断是否降级
	 */
	private Boolean isOpenManualDegrade(ClientProperties.MethodConfig methodConfig) {
		if (methodConfig != null && methodConfig.getManualDegrade() != null && methodConfig.getManualDegrade()) {
			return true;
		}
		return false;
	}

	/**
	 * 代理RPC服务接口的调用
	 *
	 * @param obj
	 *            代理对象
	 * @param method
	 *            方法
	 * @param args
	 *            入参列表
	 * @param methodProxy
	 *            代理方法
	 */
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		if (!MethodUtils.isRemoteMethod(method)) {
			return methodProxy.invoke(obj, args);
		}
		boolean isAsyncMethod = MethodUtils.isAsyncMethod(method);
		if (isAsyncMethod) {
			method = MethodUtils.getRemoteMethod(serviceClass, method);
		}
		CallContext callContext = callContextManager.getCallContext(method);
		CallContext.setCurrentCallContext(callContext);
		ClientProperties.MethodConfig methodConfig = callContextManager.getMethodConfig(callContext);
		int retryCount = getRetryCount(methodConfig);
		return invokeWithCat(method, args, isAsyncMethod, callContext, methodConfig, retryCount);
	}

	private Object invokeWithCat(Method method, Object[] args, boolean isAsyncMethod, CallContext callContext, ClientProperties.MethodConfig methodConfig, int retryCount) throws Throwable {
		MethodDescriptor methodDescriptor = MethodUtils.getMethodDescriptor(serviceClass, method);
		if (catGrpcClientHandler == null) {
			return invoke(method, args, isAsyncMethod, callContext, methodConfig, retryCount);
		}

		Transaction transaction = catGrpcClientHandler.newTransaction(NameUtils.getSimpleMethodName(methodDescriptor));
		try {
			Object result = invoke(method, args, isAsyncMethod, callContext, methodConfig, retryCount);
			catGrpcClientHandler.success(transaction);
			return result;
		} catch (Exception e) {
			catGrpcClientHandler.fail(transaction, e);
			throw e;
		} finally {
			catGrpcClientHandler.complete(transaction);
		}
	}

	private Object invoke(Method method, Object[] args, boolean isAsyncMethod, CallContext callContext, ClientProperties.MethodConfig methodConfig, int retryCount) throws Throwable {
		int currentRetryCount = 0;
		Throwable finalThrowable = null;
		while (currentRetryCount <= retryCount) {
			try {
				return executeClientCall(currentRetryCount, method, methodConfig, callContext, args, isAsyncMethod);
			} catch (Throwable e) {
				log.error("rpc invoke callContext: {}", callContext, GrpcUtils.parseCause(e));
				finalThrowable = e;
				if (GrpcUtils.isGrpcTimeoutException(e)) {
					currentRetryCount++;
				} else {
					break;
				}
			}
		}
		CallContext.removeCallContext();
		return fallbackHandle(callContext, finalThrowable, method, args, isAsyncMethod);
	}

	private Integer getRetryCount(ClientProperties.MethodConfig methodConfig) {
		int retryCount = 0;
		if (methodConfig != null && methodConfig.getRetryCount() != null && methodConfig.getRetryCount() > 0) {
			retryCount = methodConfig.getRetryCount();
		}
		return retryCount;
	}

	private Object executeClientCall(Integer currentRetryCount, Method method,
			ClientProperties.MethodConfig methodConfig, CallContext callContext, Object[] args, Boolean isAsyncMethod)
			throws RpcBlockException, RpcClientException {
		if (currentRetryCount > 0) {
			log.warn("方法 {} 当前第 {} 次重试", method.getName(), currentRetryCount);
		}
		if (Objects.equals(isOpenManualDegrade(methodConfig), true)) {
			publishRpcInvokeEvent(method, EventType.RPC_CLIENT_MANUAL_DEGRADE);
			throw new RpcBlockException(
					NameUtils.getMethodName(RpcConsts.RPC_CLIENT_TITLE, serviceClass.getName(), method));
		}
		MethodDescriptor methodDescriptor = MethodUtils.getMethodDescriptor(serviceClass, method);
		Channel channel = channelManager.chooseChannel(callContext);
		if (channel == null) {
			String message = String.format("serverName = %s 当前没有有效的实例", callContext.getServerName());
			publishFrameworkLogEvent(methodDescriptor, message);
			throw new RpcClientException(message);
		}
		InternalClientCalls.StubType stubType = isAsyncMethod
				? InternalClientCalls.StubType.FUTURE
				: InternalClientCalls.StubType.BLOCKING;
		ClientCall call = createClientCall(methodDescriptor,
				callContextManager.getCallTimeoutMillis(callContext, serviceCallTimeoutMillis), channel, stubType);
		if (isAsyncMethod) {
			return ClientCalls.futureUnaryCall(call, args[0]);
		}
		return ClientCalls.blockingUnaryCall(call, args[0]);
	}

	private ClientCall createClientCall(MethodDescriptor methodDescriptor, Integer callTimeoutMillis, Channel channel,
			InternalClientCalls.StubType stubType) {
		return channel.newCall(methodDescriptor,
				CallOptions.DEFAULT.withDeadlineAfter(callTimeoutMillis, TimeUnit.MILLISECONDS).withOption(INTERNAL_STUB_TYPE, stubType));
	}

	/**
	 * 回退机制处理
	 *
	 * @param callContext
	 *            调用上下文
	 * @param e
	 *            异常
	 * @param method
	 *            方法
	 * @param args
	 *            入参列表
	 */
	private Object fallbackHandle(CallContext callContext, Throwable e, Method method, Object[] args,
			Boolean isAsyncMethod) throws Throwable {
		Throwable cause = GrpcUtils.parseCause(e);
		if (cause != null) {
			e = cause;
		}
		Object fallback = callContext.getFallback();
		if (fallback == null) {
			throw e;
		}
		if (callContext.getException() == null) {
			return executeFallback(method, args, isAsyncMethod, fallback);
		}
		for (Class clazz : callContext.getException()) {
			if (clazz.isInstance(e)) {
				log.error("调用 {}: {}被执行降级，异常 {}: {}", callContext.getServerName(), callContext.getMethod().getName(),
						e.getClass().getTypeName(), e.getMessage());
				return executeFallback(method, args, isAsyncMethod, fallback);
			}
		}
		throw e;
	}

	private Object executeFallback(Method method, Object[] args, Boolean isAsyncMethod, Object fallback)
			throws IllegalAccessException, InvocationTargetException {
		publishRpcInvokeEvent(method, EventType.RPC_CLIENT_FALLBACK);
		if (isAsyncMethod) {
			return Futures.immediateFuture(method.invoke(fallback, args));
		} else {
			return method.invoke(fallback, args);
		}
	}

	private void publishRpcInvokeEvent(Method method, EventType eventType) {
		applicationEventPublisher.publishEvent(
				new RpcInvokeEvent(this, serviceClass.getTypeName(), method.getName(), MethodType.UNARY, eventType));
	}

	private void publishFrameworkLogEvent(MethodDescriptor methodDescriptor, String message) {
		String name = NameUtils.getMethodName(RpcConsts.RPC_CLIENT_TITLE, methodDescriptor);
		applicationEventPublisher.publishEvent(new FrameworkLogEvent(this, RpcConsts.RPC_CLIENT_TITLE, name, message));
	}
}
