package com.jindi.infra.grpc.server;

import com.jindi.infra.grpc.Infra;
import com.jindi.infra.grpc.RpcProperties;
import com.jindi.infra.grpc.TycExtendGrpc;
import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.util.ACUtils;
import com.jindi.infra.grpc.util.ClassUtils;
import com.jindi.infra.grpc.util.ExecutorsUtils;
import com.jindi.infra.grpc.util.MethodUtils;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Grpc服务器
 */
@Slf4j
public class GrpcServiceProxy {

	private static final AtomicBoolean OPEN = new AtomicBoolean(false);
	private static final int BOSS_N_THREADS = 1;
	private static final String GRPC_SERVER_INVOKE = "grpc-server-invoke";

	private Set<BindableService> bindableServices = Collections.synchronizedSet(new HashSet<>());
	private Map<Class<?>, BindableService> serviceClassBindableServiceMap = Collections
			.synchronizedMap(new HashMap<>());

	/**
	 * 配置属性
	 */
	@Autowired
	private RpcProperties rpcProperties;

	/**
	 * grpc原生服务器
	 */
	private Server server;

	public <T> void register(Class<?> serviceClass, BindableService bindableService) {
		Objects.requireNonNull(bindableService);
		bindableServices.add(bindableService);
		serviceClassBindableServiceMap.put(serviceClass.getSuperclass(), bindableService);
	}

	/**
	 * 启动服务
	 *
	 * @throws Throwable
	 */
	public void start() throws Throwable {
		if (!OPEN.compareAndSet(false, true)) {
			log.warn("RPC 服务端已经被启动");
			return;
		}
		if (CollectionUtils.isEmpty(bindableServices)) {
			return;
		}
		startServer();
		cacheServiceInfo();
	}

	private void cacheServiceInfo() {
		RpcConsts.SERVICE_INFO = getServiceInfo();
	}

	private void startServer() throws Exception {
		log.info("RPC 服务端正在被启动");
		NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(rpcProperties.getServer().getPort())
				.channelType(NioServerSocketChannel.class).bossEventLoopGroup(new NioEventLoopGroup(BOSS_N_THREADS))
				.executor(ExecutorsUtils.newThreadPool(rpcProperties.getServer().getThreadPool(), rpcProperties.getServer().getWorkerThreads(), GRPC_SERVER_INVOKE))
				.workerEventLoopGroup(new NioEventLoopGroup());
		if (rpcProperties.getSecurity().getEnable()) {
			serverBuilder = serverBuilder.useTransportSecurity(
					GrpcServiceProxy.class.getResourceAsStream(rpcProperties.getSecurity().getCertChain()),
					GrpcServiceProxy.class.getResourceAsStream(rpcProperties.getSecurity().getPrivateKey()));
		}
		addService(serverBuilder);
		addInterceptor(serverBuilder);
		server = serverBuilder.build().start();
		log.info("RPC 服务端 启动成功 监听 {} 端口", rpcProperties.getServer().getPort());
	}

	private void addService(NettyServerBuilder serverBuilder) {
		for (BindableService bindableService : bindableServices) {
			serverBuilder.addService(bindableService);
		}
		// 心跳
		serverBuilder.addService(new TycExtendGrpc.TycExtendImplBase() {
			@Override
			public void ping(Infra.Empty request, StreamObserver<Infra.Empty> responseObserver) {
				responseObserver.onNext(request);
				responseObserver.onCompleted();
			}
		});
		// 开启反射
		serverBuilder.addService(ProtoReflectionService.newInstance());
	}

	private void addInterceptor(NettyServerBuilder serverBuilder) {
		List<ServerInterceptor> serverInterceptors = ACUtils.getBeansOfType(ServerInterceptor.class);
		if (!CollectionUtils.isEmpty(serverInterceptors)) {
			AnnotationAwareOrderComparator.sort(serverInterceptors);
			Collections.reverse(serverInterceptors);
			for (ServerInterceptor serverInterceptor : serverInterceptors) {
				serverBuilder.intercept(serverInterceptor);
			}
		}
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
		if (server != null) {
			server.awaitTermination(rpcProperties.getServer().getAwaitTerminationSecond(), TimeUnit.SECONDS);
		}
	}

	/**
	 * 获取所有的RPC服务类
	 *
	 * @return
	 */
	public List<Class<BindableService>> getAllServiceClass() {
		if (CollectionUtils.isEmpty(serviceClassBindableServiceMap)) {
			return Collections.emptyList();
		}
		return new ArrayList(serviceClassBindableServiceMap.keySet());
	}

	/**
	 * 获取RPC服务类对应的对象
	 *
	 * @param serviceClass
	 *            RPC服务类
	 * @return
	 */
	public BindableService getBindableServiceByServiceClass(Class<?> serviceClass) {
		return serviceClassBindableServiceMap.get(serviceClass);
	}

	/**
	 * 在nacos中展示服务信息，对nacos进行增强
	 */
	private String getServiceInfo() {
		try {
			List<Class<BindableService>> serviceClasses = getAllServiceClass();
			if (CollectionUtils.isEmpty(serviceClasses)) {
				return "";
			}
			serviceClasses.sort(Comparator.comparing(serviceClass -> serviceClass.getSimpleName()));
			StringBuilder sb = new StringBuilder();
			for (Class serviceClass : serviceClasses) {
				sb.append(serviceClass.getName()).append("#");
				List<Method> methodList = Arrays.asList(serviceClass.getDeclaredMethods());
				methodList.sort(Comparator.comparing(method -> method.getName()));
				for (Method method : methodList) {
					if (MethodUtils.isAsyncMethod(method) || !MethodUtils.isRemoteMethod(method)) {
						continue;
					}
					if (Objects.equals("void", method.getReturnType().getTypeName())) {
						sb.append("void");
					} else {
						sb.append(ClassUtils.forName(method.getReturnType().getTypeName()).getSimpleName());
					}
					sb.append(" ").append(method.getName()).append("(");
					for (Class parameterType : method.getParameterTypes()) {
						sb.append(parameterType.getSimpleName());
						sb.append(",");
					}
					if (!ArrayUtils.isEmpty(method.getParameterTypes())) {
						sb = new StringBuilder(sb.substring(0, sb.length() - 1));
					}
					sb.append(")");
					sb.append("@");
				}
				sb = sb.replace(sb.length() - 1, sb.length(), "-");
			}
			return sb.substring(0, sb.length() - 1);
		} catch (Exception e) {
			log.error("解析grpc服务及方法列表失败", e);
		}
		return "";
	}

}
