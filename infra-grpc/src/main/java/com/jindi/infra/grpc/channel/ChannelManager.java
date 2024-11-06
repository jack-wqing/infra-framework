package com.jindi.infra.grpc.channel;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.exception.IllegalRegionException;
import com.jindi.infra.grpc.RpcProperties;
import com.jindi.infra.grpc.client.ClientProperties;
import com.jindi.infra.grpc.extension.DiscoveryProvider;
import com.jindi.infra.grpc.extension.Node;
import com.jindi.infra.grpc.model.CallContext;
import com.jindi.infra.grpc.util.ACUtils;
import com.jindi.infra.grpc.util.ExecutorsUtils;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.ChannelOption;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
public class ChannelManager implements Closeable {

	private static final NullManagedChannel NULL_MANAGED_CHANNEL = new NullManagedChannel();
	private static final DiscoveryProvider NULL_DISCOVERY_PROVIDER = new NullDiscoveryProvider();
	private static final String GRPC_CLIENT_INVOKE = "grpc-client-invoke";
	private final ExecutorService initChannelExecutor = ExecutorsUtils.newQueueThreadPool(ExecutorsUtils.FIXED, 4, INIT_CHANNEL_NAME);
	private static final String INIT_CHANNEL_NAME = "init-channel";
	private static final String ADDRESS_FORMAT = "%s:%d";
	private static final String LOCALHOST_9999 = "localhost:9999";
	private ExecutorService executor;
	private final Map<String, Map<Node, ManagedChannel>> channelCache = new ConcurrentHashMap<>();
	private final Object lock = new Object();
	private final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(
			Runtime.getRuntime().availableProcessors());
	private final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	}};
	private final String region;
	@Autowired
	private ObjectProvider<List<DiscoveryProvider>> discoveryProviderObjectProvider;
	@Autowired
	private RpcProperties rpcProperties;
	private final AtomicReference<DiscoveryProvider> discoveryProviderReference = new AtomicReference<>(null);
	private PingManager pingManager;

	public ChannelManager(String region) {
		this.region = region;
	}

	@PostConstruct
	public void init() {
		this.executor = ExecutorsUtils.newThreadPool(rpcProperties.getClient().getThreadPool(), rpcProperties.getClient().getWorkerThreads(), GRPC_CLIENT_INVOKE);
		this.pingManager = new PingManager(this, rpcProperties);
		this.pingManager.start();
	}

	public boolean isNullChannel(ManagedChannel channel) {
		return channel == null || channel == NULL_MANAGED_CHANNEL;
	}

	public boolean isNullDiscoveryProvider(DiscoveryProvider discoveryProvider) {
		return discoveryProvider == null || discoveryProvider == NULL_DISCOVERY_PROVIDER;
	}

	public DiscoveryProvider getDiscoveryProvider() {
		if (discoveryProviderReference.get() != null) {
			return discoveryProviderReference.get();
		}
		List<DiscoveryProvider> discoveryProviders = discoveryProviderObjectProvider.getIfAvailable();
		if (CollectionUtils.isEmpty(discoveryProviders) || StringUtils.isBlank(region)) {
			discoveryProviderReference.compareAndSet(null, NULL_DISCOVERY_PROVIDER);
			return NULL_DISCOVERY_PROVIDER;
		}
		for (DiscoveryProvider discoveryProvider : discoveryProviders) {
			if (Objects.equals(discoveryProvider.getRegion(), region)) {
				discoveryProviderReference.compareAndSet(null, discoveryProvider);
				return discoveryProvider;
			}
		}
		throw new IllegalRegionException(String.format("不支持的 region = %s 异常", region));
	}

	private Integer chooseRandomInt(int serverCount) {
		return ThreadLocalRandom.current().nextInt(serverCount);
	}

	/**
	 * 从nodeChannel缓存中，随机选个一个有效的channel返回
	 */
	private Channel randomChooseChannel(String serverName, Map<Node, ManagedChannel> nodeChannel) {
		List<ManagedChannel> activeChannel = new ArrayList<>(nodeChannel.size());
		for (Map.Entry<Node, ManagedChannel> entry : nodeChannel.entrySet()) {
			ManagedChannel channel = entry.getValue();
			if (isNullChannel(channel)) {
				continue;
			}
			if (channel.getState(true) == ConnectivityState.READY) {
				activeChannel.add(channel);
			} else {
                log.warn("当前可用实例 serverName = {} node = {}, state = {}", serverName,
                    InnerJSONUtils.toJSONString(entry.getKey()), channel.getState(true).name());
            }
		}
		return activeChannel.isEmpty() ? null : activeChannel.get(chooseRandomInt(activeChannel.size()));
	}

	/**
	 * 初始化链接
	 *
	 * @param serverName
	 *            下游服务名称
	 */
	public synchronized void initChannel(String serverName) {
		Objects.requireNonNull(serverName);
		if (!channelCache.containsKey(serverName)) {
			channelCache.put(serverName, new ConcurrentHashMap<>());
		}
		if (!channelCache.get(serverName).isEmpty()) {
			return;
		}
		Node node = getNode(serverName);
		if (node == null) {
			log.warn("注册中心没有发现 serverName = {} 的实例", serverName);
			return;
		}
		try {
			channelCache.get(serverName).put(node, createChannel(serverName, node));
			log.info("创建节点 serverName = {} host = {} port = {} 链接成功", serverName, node.getHost(), node.getPort());
		} catch (Throwable e) {
			log.error(String.format("创建节点 serverName = %s host = %s port = %d 链接失败", serverName, node.getHost(),
					node.getPort()), e);
		}
	}

	/**
	 * 根据serverName从注册中心选择一个instance，并封装node
	 */
	private Node getNode(String serverName) {
		DiscoveryProvider discoveryProvider = getDiscoveryProvider();
		ClientProperties.Server server = rpcProperties.getClient().getServerNameServer().get(serverName);
		if ((server != null && server.getDirect()) || isNullDiscoveryProvider(discoveryProvider)) {
			String target = server != null && StringUtils.isNotBlank(server.getTarget())
					? server.getTarget()
					: LOCALHOST_9999;
			String[] ss = StringUtils.split(target, ":");
			return new Node(ss[0], Integer.parseInt(ss[1]));
		} else {
			return discoveryProvider.chooseServer(serverName);
		}
	}

	/**
	 * 获取一个grpc调用Channel 如果选择的Node建立连接，则直接返回；如果未建立连接，就异步建立channel，并随机返回一个可用的channel
	 *
	 * @param callContext
	 *            下游服务名
	 */
	public Channel chooseChannel (CallContext callContext) {
		return chooseChannel(null, callContext, false);
	}

	public Channel chooseChannel(String serverName, CallContext callContext, Boolean ping) {
		if (StringUtils.isBlank(serverName)) {
			serverName = callContext.getServerName();
		}
		Objects.requireNonNull(serverName);
		Node node = getNode(serverName);
		if (node == null) {
			if (ping) {
				log.debug("注册中心没有发现 serverName = {} 的实例", serverName);
			} else {
				log.warn("注册中心没有发现 serverName = {} 的实例", serverName);
			}
			return null;
		}
        if (callContext != null) {
            callContext.setNode(node);
        }
		Map<Node, ManagedChannel> nodeChannel = channelCache.get(serverName);
		if (!isNullChannel(nodeChannel.get(node))) {
			// 如果当前node是有效的channel，就直接返回
			return nodeChannel.get(node);
		}
		createNodeChannel(serverName, ping, node, nodeChannel);
		return randomChooseChannel(serverName, nodeChannel);
	}

	private void createNodeChannel(String serverName, Boolean ping, Node node, Map<Node, ManagedChannel> nodeChannel) {
		if (!nodeChannel.containsKey(node)) {
			synchronized (lock) {
				if (!nodeChannel.containsKey(node)) {
					/** 防止，重复阻塞提交创建链接，添加null做为标志 */
					nodeChannel.put(node, NULL_MANAGED_CHANNEL);
					initChannelExecutor.submit(() -> {
						try {
							nodeChannel.put(node, createChannel(serverName, node));
							log.info("创建节点 serverName = {} host = {} port = {} 链接成功", serverName, node.getHost(),
									node.getPort());
						} catch (Throwable e) {
							/** 防止，重复阻塞提交创建链接，清除null标志 */
							nodeChannel.remove(node);
							if (ping) {
								log.debug("创建节点连接失败 serverName = {} host = {} port = {}", serverName, node.getHost(),
										node.getPort(), e);
							} else {
								log.error("创建节点连接失败 serverName = {} host = {} port = {}", serverName, node.getHost(),
										node.getPort(), e);
							}
						}
					});
				}
			}
		}
	}

	/**
	 * 创建链接
	 */
	private synchronized ManagedChannel createChannel(String serverName, Node node) throws Throwable {
		NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder
				.forTarget(String.format(ADDRESS_FORMAT, node.getHost(), node.getPort()))
				.withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, rpcProperties.getClient().getConnectTimeoutMillis())
				.channelType(NioSocketChannel.class).eventLoopGroup(nioEventLoopGroup)
				.executor(executor)
				.maxInboundMessageSize(rpcProperties.getMaxPayLoad());
		if (rpcProperties.getSecurity().getEnable()) {
			nettyChannelBuilder = nettyChannelBuilder.useTransportSecurity()
					.sslContext(GrpcSslContexts.forClient().trustManager(trustAllCerts[0]).build())
					.negotiationType(NegotiationType.TLS);
		} else {
			nettyChannelBuilder = nettyChannelBuilder.usePlaintext();
		}
		List<ClientInterceptor> clientInterceptors = ACUtils.getBeansOfType(ClientInterceptor.class);
		if (!CollectionUtils.isEmpty(clientInterceptors)) {
			AnnotationAwareOrderComparator.sort(clientInterceptors);
			Collections.reverse(clientInterceptors);
			nettyChannelBuilder.intercept(clientInterceptors.toArray(new ClientInterceptor[clientInterceptors.size()]));
		}
		ManagedChannel channel = nettyChannelBuilder.build();
		try {
			pingManager.validateChannel(serverName, node, channel);
		} catch (Throwable e) {
			channel.shutdown();
			throw e;
		}
		return channel;
	}

	@Override
	public void close() {
		log.warn("grpc ChannelManager close start -------");
		pingManager.close();
		if (channelCache.isEmpty()) {
			log.warn("grpc ChannelManager close end -------");
			return;
		}
		channelCache.values().stream()
				.filter(nodeManagedChannelMap -> nodeManagedChannelMap != null && !nodeManagedChannelMap.isEmpty())
				.flatMap(
						(Function<Map<Node, ManagedChannel>, Stream<ManagedChannel>>) nodeManagedChannelMap -> nodeManagedChannelMap
								.values().stream())
				.forEach(managedChannel -> {
					if (managedChannel == null) {
						return;
					}
					try {
						managedChannel.shutdown();
					} catch (Throwable e) {
						log.error("关闭channel", e);
					}
				});
		log.warn("grpc ChannelManager close end -------");
	}

	public Map<String, Map<Node, ManagedChannel>> getChannelCache() {
		return this.channelCache;
	}

	public String getRegion () {
		return this.region;
	}
}
