package com.jindi.infra.grpc.pure.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.grpc.pure.GrpcClientPool;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractBlockingStub;

public class SimpleGrpcTools {

	private static ExecutorService executorService = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

	/**
	 * @param grpcClientPool
	 *            客户端缓存池（全局唯一）
	 * @param serverName
	 *            调用应用名
	 * @param clazz
	 *            调用类(pb定义的服务名 + BlockingStub)
	 * @param <T>
	 * @return
	 * @throws Throwable
	 */
	public static <T extends AbstractBlockingStub<T>> T createBlockingStub(GrpcClientPool grpcClientPool,
			String serverName, Class<T> clazz) throws Throwable {
		ManagedChannel managedChannel = grpcClientPool.createManagedChannel(String.format("nacos://%s", serverName));
		String blockingStub = clazz.getTypeName();
		Class grpcClass = ClassUtils.getClass(StringUtils.substringBeforeLast(blockingStub, "$"));
		Object blockingStubBean = grpcClass.getMethod("newBlockingStub", Channel.class).invoke(null, managedChannel);
		blockingStubBean = blockingStubBean.getClass().getMethod("withExecutor", Executor.class)
				.invoke(blockingStubBean, executorService);
		return (T) blockingStubBean;
	}
}
