package com.jindi.infra.grpc.channel;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.grpc.Infra;
import com.jindi.infra.grpc.RpcProperties;
import com.jindi.infra.grpc.TycExtendGrpc;
import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.extension.DiscoveryProvider;
import com.jindi.infra.grpc.extension.Node;
import com.jindi.infra.grpc.util.ExecutorsUtils;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.InternalClientCalls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PingManager {

	private static final int LOOP_COUNT = 3;
	private static final int SLEEP_MILLIS = 10;
	private static final int INITIAL_DELAY_MILLISECONDS = 60000;
	private static final int PERIOD_MILLISECONDS = 3000;
	private static final String PING_EXECUTOR_NAME = "ping-executor";
	private static final String RPC_CLIENT_PING_SCHEDULED_NAME = "rpc-client-ping-scheduled";
	private static final String INTERNAL_STUB_TYPE = "internal-stub-type";
	private final ExecutorService pingExecutor = ExecutorsUtils.newQueueThreadPool(ExecutorsUtils.FIXED, 4, PING_EXECUTOR_NAME);
	private final ScheduledExecutorService scheduledExecutorService = Executors
			.newSingleThreadScheduledExecutor(r -> new Thread(r, RPC_CLIENT_PING_SCHEDULED_NAME));
	private DiscoveryProvider discoveryProvider;
	private Map<String, Map<Node, ManagedChannel>> channelCache;
	private ChannelManager channelManager;
	private RpcProperties rpcProperties;

	public PingManager(ChannelManager channelManager, RpcProperties rpcProperties) {
		this.channelManager = channelManager;
		this.rpcProperties = rpcProperties;
		this.discoveryProvider = channelManager.getDiscoveryProvider();
		this.channelCache = channelManager.getChannelCache();
	}

	public void start() {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			for (Map.Entry<String, Map<Node, ManagedChannel>> entry : channelCache.entrySet()) {
				String serverName = entry.getKey();
				check(serverName);
			}
		}, INITIAL_DELAY_MILLISECONDS, PERIOD_MILLISECONDS, TimeUnit.MILLISECONDS);
	}

	private void check(String serverName) {
		Map<Node, ManagedChannel> nodeChannel = channelCache.get(serverName);
		ManagedChannel channel = (ManagedChannel) channelManager.chooseChannel(serverName, null, true);
		if (channelManager.isNullChannel(channel)) {
			return;
		}
		Node node = null;
		List<Node> liveNodes = null;
		if (!channelManager.isNullDiscoveryProvider(discoveryProvider)) {
			liveNodes = discoveryProvider.getAllNodes(serverName);
		}
		for (Map.Entry<Node, ManagedChannel> channelEntry : nodeChannel.entrySet()) {
			if (channelEntry.getValue() == channel) {
				node = channelEntry.getKey();
				continue;
			}
			// 清除掉无效的缓存链接；如果没有活着的节点时，不清理缓存（系统保护策略）
			if (!CollectionUtils.isEmpty(liveNodes) && !liveNodes.contains(channelEntry.getKey())) {
				ManagedChannel deathChannel = nodeChannel.remove(channelEntry.getKey());
				closeManagedChannel(deathChannel, serverName, channelEntry.getKey());
			}
		}
		if (node == null) {
			return;
		}
		log.debug("定期检测: 当前可用实例 serverName = {} node = {}, state = {}", serverName, InnerJSONUtils.toJSONString(node),
				channel.getState(true).name());
		if (!health(serverName, channel, node)) {
			log.debug("定期检测: 清除节点 serverName = {} node = {}, state = {}", serverName, InnerJSONUtils.toJSONString(node),
					channel.getState(true).name());
			channelCache.get(serverName).remove(node);
			closeManagedChannel(channel, serverName, node);
		}
	}

	/**
	 * 在包活心跳逻辑中判断链接是否健康。解决链接断开重链有一段冷却时间
	 */
	private boolean health(String serverName, ManagedChannel channel, Node node) {
		for (int i = 0; i < LOOP_COUNT; i++) {
			try {
				ping(serverName, channel, node);
				return true;
			} catch (Throwable e) {
				try {
					Thread.sleep(SLEEP_MILLIS);
				} catch (InterruptedException interruptedException) {
					log.error("sleep interrupted", interruptedException);
				}
			}
		}
		return false;
	}

	/**
	 * 心跳
	 */
	private void ping(String serverName, ManagedChannel channel, Node node) {
		ClientCall<Infra.Empty, Infra.Empty> call = channel.newCall(TycExtendGrpc.getPingMethod(),
				CallOptions.DEFAULT.withExecutor(pingExecutor)
						.withDeadlineAfter(rpcProperties.getClient().getPingCallTimeoutMillis(), TimeUnit.MILLISECONDS)
						.withOption(CallOptions.Key.create(INTERNAL_STUB_TYPE), InternalClientCalls.StubType.BLOCKING));
		try {
			RpcConsts.GATEWAY_ROUTING_VALUE.set(serverName);
			ClientCalls.blockingUnaryCall(call, Infra.Empty.newBuilder().build());
			log.debug("心跳 {}:{} 成功", node.getHost(), node.getPort());
		} catch (Throwable e) {
			log.debug("ping", e);
			throw e;
		} finally {
			RpcConsts.GATEWAY_ROUTING_VALUE.set(null);
		}
	}

	public void validateChannel(String serverName, Node node, ManagedChannel channel) throws Throwable {
		Throwable finalThrowable = null;
		for (int i = 0; i < LOOP_COUNT; i++) {
			try {
				ping(serverName, channel, node);
				return;
			} catch (Throwable e) {
				finalThrowable = e;
			}
		}
		if (finalThrowable != null) {
			throw finalThrowable;
		}
	}

	/**
	 * 主动关闭channel；否则创建channel时会偶发的抛出RuntimeException
	 *
	 * @param deathChannel
	 */
	private void closeManagedChannel(ManagedChannel deathChannel, String serverName, Node node) {
		if (deathChannel == null) {
			return;
		}
		try {
			log.info("定期检测: 清除节点 serverName = {} host = {} port = {}", serverName, node.getHost(), node.getPort());
			deathChannel.shutdown();
		} catch (Throwable e) {
			log.error("关闭channel", e);
		}
	}

	public void close() {
		scheduledExecutorService.shutdownNow();
		try {
			Thread.sleep(PERIOD_MILLISECONDS);
		} catch (InterruptedException e) {
		}
	}

}
