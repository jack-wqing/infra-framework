package com.jindi.infra.grpc.pure.loadbalance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jindi.infra.grpc.pure.constant.LoadBalanceConsts.WEIGHT;
import static io.grpc.ConnectivityState.*;

import java.util.*;

import com.alibaba.nacos.client.naming.utils.Pair;

import com.jindi.infra.grpc.pure.constant.DiscoveryConsts;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于内置的RoundRobinLoadBalancer负载均衡器修改；因为grpc自身对扩展不是推荐
 */
@Slf4j
public final class NacosLoadBalancer extends LoadBalancer {

	static final Attributes.Key<Ref<ConnectivityStateInfo>> STATE_INFO = Attributes.Key.create("state-info");
	private static final Status EMPTY_OK = Status.OK.withDescription("no subChannels ready");
	private final Helper helper;
	private final Map<EquivalentAddressGroup, Subchannel> subChannels = new HashMap<>();
	private ConnectivityState currentState;


	public NacosLoadBalancer(Helper helper) {
		this.helper = checkNotNull(helper, "helper");
	}

	private List<Subchannel> filterNonFailingSubChannels(Collection<Subchannel> subChannels) {
		List<Subchannel> readySubChannels = new ArrayList<>(subChannels.size());
		for (Subchannel subChannel : subChannels) {
			if (getSubChannelStateInfoRef(subChannel).value.getState() == READY) {
				readySubChannels.add(subChannel);
			}
		}
		return readySubChannels;
	}

	private Map<EquivalentAddressGroup, EquivalentAddressGroup> stripAttrs(List<EquivalentAddressGroup> groupList) {
		Map<EquivalentAddressGroup, EquivalentAddressGroup> map = new HashMap<>(groupList.size() * 2);
		for (EquivalentAddressGroup group : groupList) {
			map.put(stripAttrs(group), group);
		}
		return map;
	}

	private EquivalentAddressGroup stripAttrs(EquivalentAddressGroup eag) {
		return new EquivalentAddressGroup(eag.getAddresses());
	}

	private Ref<ConnectivityStateInfo> getSubChannelStateInfoRef(Subchannel subchannel) {
		return subchannel.getAttributes().get(STATE_INFO);
	}

	private <T> Set<T> setsDifference(Set<T> a, Set<T> b) {
		Set<T> aCopy = new HashSet<>(a);
		aCopy.removeAll(b);
		return aCopy;
	}

	@Override
	public void handleResolvedAddresses(ResolvedAddresses resolvedAddresses) {
		List<EquivalentAddressGroup> servers = resolvedAddresses.getAddresses();
		Set<EquivalentAddressGroup> currentAddresses = subChannels.keySet();
		Map<EquivalentAddressGroup, EquivalentAddressGroup> addresses = stripAttrs(servers);
		Set<EquivalentAddressGroup> removedAddresses = setsDifference(currentAddresses, addresses.keySet());

		for (Map.Entry<EquivalentAddressGroup, EquivalentAddressGroup> entry : addresses.entrySet()) {
			EquivalentAddressGroup strippedAddressGroup = entry.getKey();
			EquivalentAddressGroup originalAddressGroup = entry.getValue();
			Subchannel existingSubChannel = subChannels.get(strippedAddressGroup);
			if (existingSubChannel != null) {
				existingSubChannel.updateAddresses(Collections.singletonList(originalAddressGroup));
				continue;
			}
			Attributes.Builder attributesBuilder = Attributes.newBuilder().set(STATE_INFO,
					new Ref<>(ConnectivityStateInfo.forNonError(IDLE)));
			final Subchannel subchannel = helper.createSubchannel(CreateSubchannelArgs.newBuilder()
					.setAddresses(originalAddressGroup).setAttributes(attributesBuilder.build()).build());
			subchannel.start(state -> processSubChannelState(subchannel, state));
			subChannels.put(strippedAddressGroup, subchannel);
			subchannel.requestConnection();
		}
		ArrayList<Subchannel> removedSubChannels = new ArrayList<>();
		for (EquivalentAddressGroup addressGroup : removedAddresses) {
			removedSubChannels.add(subChannels.remove(addressGroup));
		}

		updateBalancingState();

		for (Subchannel removedSubChannel : removedSubChannels) {
			removedSubChannel.shutdown();
			getSubChannelStateInfoRef(removedSubChannel).value = ConnectivityStateInfo.forNonError(SHUTDOWN);
		}
	}

	@Override
	public void handleNameResolutionError(Status error) {
		if (currentState != READY) {
			updateBalancingState(TRANSIENT_FAILURE, new EmptyPicker(error));
		}
	}

	private void processSubChannelState(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
		if (subChannels.get(stripAttrs(subchannel.getAddresses())) != subchannel) {
			return;
		}
		if (stateInfo.getState() == TRANSIENT_FAILURE || stateInfo.getState() == IDLE) {
			helper.refreshNameResolution();
		}
		if (stateInfo.getState() == IDLE) {
			subchannel.requestConnection();
		}
		Ref<ConnectivityStateInfo> subChannelStateRef = getSubChannelStateInfoRef(subchannel);
		if (subChannelStateRef.value.getState().equals(TRANSIENT_FAILURE)) {
			if (stateInfo.getState().equals(CONNECTING) || stateInfo.getState().equals(IDLE)) {
				return;
			}
		}
		subChannelStateRef.value = stateInfo;
		updateBalancingState();
	}

	@Override
	public void shutdown() {
		for (Subchannel subchannel : subChannels.values()) {
			subchannel.shutdown();
			getSubChannelStateInfoRef(subchannel).value = ConnectivityStateInfo.forNonError(SHUTDOWN);
		}
		subChannels.clear();
	}

	private void updateBalancingState() {
		List<Subchannel> activeList = filterNonFailingSubChannels(subChannels.values());
		if (activeList.isEmpty()) {
			boolean isConnecting = false;
			Status status = EMPTY_OK;
			for (Subchannel subchannel : subChannels.values()) {
				ConnectivityStateInfo stateInfo = getSubChannelStateInfoRef(subchannel).value;
				if (stateInfo.getState() == CONNECTING || stateInfo.getState() == IDLE) {
					isConnecting = true;
				}
				status = stateInfo.getStatus();
			}
			updateBalancingState(isConnecting ? CONNECTING : TRANSIENT_FAILURE, new EmptyPicker(status));
			return;
		}
		List<SubChannelInfo> subChannelInfos = new ArrayList<>();
		for (Subchannel subchannel : activeList) {
			SubChannelInfo subChannelInfo = getSubChannelInfo(subchannel);
			subChannelInfos.add(subChannelInfo);
		}
		ReadyPicker readyPicker = new ReadyPicker(subChannelInfos);
		updateBalancingState(READY, readyPicker);
	}

	private SubChannelInfo getSubChannelInfo(Subchannel subChannel) {
		double weight = WEIGHT;
		Long registrationTime = null;
		String ip = null;
		try {
			if (subChannel.getAddresses() != null && subChannel.getAddresses().getAttributes() != null) {
				Object weightObj = subChannel.getAddresses().getAttributes().get(DiscoveryConsts.WEIGHT_ATTRIBUTES_KEY);
				if (weightObj != null) {
					weight = (double) weightObj;
				}
				Object registrationTimeObj = subChannel.getAddresses().getAttributes().get(DiscoveryConsts.REGISTRATION_TIME_ATTRIBUTES_KEY);
				if (registrationTimeObj != null) {
					registrationTime = Long.parseLong(String.valueOf(registrationTimeObj));
				}
				ip = subChannel.getAddresses().getAddresses().get(0).toString();
			}
		} catch (Exception e) {
			log.error("getSubChannelInfo error", e);
		}
		return new SubChannelInfo(subChannel, weight, registrationTime, ip);
	}

	private void updateBalancingState(ConnectivityState state, WeightRandomPicker picker) {
		currentState = state;
		helper.updateBalancingState(state, picker);
	}

}
