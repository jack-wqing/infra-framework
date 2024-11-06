package com.jindi.infra.grpc.pure.loadbalance;

import static com.jindi.infra.grpc.pure.constant.LoadBalanceConsts.UPTIME_DEGRADATION_LIMIT;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;

import io.grpc.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadyPicker extends WeightRandomPicker {

	private final List<SubChannelInfo> subChannelInfos;

	public ReadyPicker(List<SubChannelInfo> subChannelInfos) {
		this.subChannelInfos = subChannelInfos;
	}

	@Override
	public LoadBalancer.PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
		List<Pair<LoadBalancer.Subchannel>> subChannelWithWeight = buildSubChannelWithWeight();
		Chooser<String, LoadBalancer.Subchannel> vipChooser = new Chooser<>("infra-grpc-pure");
		vipChooser.refresh(subChannelWithWeight);
		return LoadBalancer.PickResult.withSubchannel(vipChooser.randomWithWeight());
	}

	@Override
	public boolean isEquivalentTo(WeightRandomPicker picker) {
		return false;
	}

	private List<Pair<LoadBalancer.Subchannel>> buildSubChannelWithWeight() {
		List<Pair<LoadBalancer.Subchannel>> subChannelWithWeight = new ArrayList<>(subChannelInfos.size());
		for (SubChannelInfo subChannelInfo : subChannelInfos) {
			double weight = climbingWeight(subChannelInfo);
			subChannelWithWeight.add(new Pair<>(subChannelInfo.getSubChannel(), weight));
		}
		return subChannelWithWeight;
	}

	/**
	 * 计算爬坡权重
	 */
	private double climbingWeight(SubChannelInfo subChannelInfo) {
		double weight = subChannelInfo.getInitWeight();
		if (subChannelInfo.getRegistrationTime() != null) {
			long uptime = System.currentTimeMillis() - subChannelInfo.getRegistrationTime();
			if (uptime > 0 && uptime < UPTIME_DEGRADATION_LIMIT) {
				weight = weight * uptime / UPTIME_DEGRADATION_LIMIT;
				recordLog(subChannelInfo, uptime, weight);
			}
		}
		return weight;
	}

	private void recordLog(SubChannelInfo subChannelInfo, long uptime, double weight) {
		try {
			String serviceName = subChannelInfo.getSubChannel().asChannel().authority();
			log.warn("爬坡 service: {}, uptime: {}, weight: {}, ip: {}", serviceName, uptime, weight, subChannelInfo.getIp());
		} catch (Exception e) {
			log.error("weight log error", e);
		}
	}
}
