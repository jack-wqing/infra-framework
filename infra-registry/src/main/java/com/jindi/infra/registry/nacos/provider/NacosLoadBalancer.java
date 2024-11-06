package com.jindi.infra.registry.nacos.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.jindi.infra.core.constants.LaneTagThreadLocal;
import com.jindi.infra.grpc.extension.DiscoveryProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;
import com.jindi.infra.grpc.extension.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NacosLoadBalancer {

	private static final String INFRA_GRPC = "infra-grpc";
	private static final String SEPARATOR_CHARS = ",";
	private static final String REGISTRATION_TIME = "registrationTime";
	/**
	 * 爬坡，120秒
	 */
	private static final Double UPTIME_DEGRADATION_LIMIT = 120000.0;

	/**
	 * 获取节点
	 *
	 * @param tag
	 * @param instances
	 * @return
	 */
	// TODO: 2023/3/30 应该参考dubbo拆成两部分
	// 1.filter 完成实例过滤(例如根据泳道标识做实例过滤)
	// 2.loadbalance 对过滤以后得实例做负载均衡(例如根据启动时间做带爬坡的负载均衡)
	public Node choose(String tag, String serverName, List<Instance> instances) {
		List<Pair<Instance>> tagMatchedInstances = new ArrayList<>(instances.size());
		List<Pair<Instance>> unTagInstances = new ArrayList<>(instances.size());
		for (Instance instance : instances) {
			// 判断实例运行状态
			if (!instance.isEnabled() || !instance.isHealthy() || instance.getWeight() <= 0) {
				continue;
			}
			double weight = getWeight(instance);
			String laneTag = getTag(instance.getMetadata());
			if (StringUtils.isBlank(laneTag)) {
				unTagInstances.add(new Pair<>(instance, weight));
			} else if (StringUtils.isNotBlank(tag) && laneTag.contains(tag)) {
				tagMatchedInstances.add(new Pair<>(instance, weight));
			}
		}
		Instance instance = null;
		if (StringUtils.isNotBlank(tag)) {
			instance = selectByRandomWeight(tagMatchedInstances);
			if (instance == null) {
				log.debug("有泳道标识,但是grpc未找到具有标识的服务端,将会尝试兜底走骨干链路, tag:{}, appName:{}", tag, serverName);
			} else {
				log.debug("有泳道标识 && grpc找到了具有标识的服务端, tag:{}, appName:{}, address:{}", tag, serverName, instance.getIp());
			}
		}
		if (instance == null) {
			instance = selectByRandomWeight(unTagInstances);
		}
		return instance == null ? null : new Node(instance.getIp(), instance.getPort());
	}

	private double getWeight(Instance instance) {
		double weight = instance.getWeight();
		Map<String, String> metadata = instance.getMetadata();
		// 爬坡
		if (metadata != null && metadata.containsKey(REGISTRATION_TIME)) {
			long uptime = System.currentTimeMillis() - Long.parseLong(metadata.get(REGISTRATION_TIME));
			if (uptime > 0 && uptime < UPTIME_DEGRADATION_LIMIT) {
				weight = instance.getWeight() * uptime / UPTIME_DEGRADATION_LIMIT;
			}
		}
		return weight;
	}

	/**
	 * 筛选出打上指定标签实例
	 *
	 * @param tag
	 * @param laneTag
	 * @return
	 */
	private boolean isLaneInstance(String tag, String laneTag) {
		if (StringUtils.isBlank(laneTag)) {
			return false;
		}
		String[] tags = StringUtils.split(laneTag, SEPARATOR_CHARS);
		return ArrayUtils.contains(tags, tag);
	}

	private String getTag(Map<String, String> metadata) {
		if (metadata == null) {
			return null;
		}
		String tagStr = metadata.get(LaneTagThreadLocal.LANE_TAG_KEY);
		if (StringUtils.isBlank(tagStr)) {
			return null;
		}
		return tagStr;
	}

	/**
	 * 权重随机
	 *
	 * @return
	 */
	private Instance selectByRandomWeight(List<Pair<Instance>> instancesWithWeight) {
		if (CollectionUtils.isEmpty(instancesWithWeight)) {
			return null;
		}
		Chooser<String, Instance> vipChooser = new Chooser<>(INFRA_GRPC);
		vipChooser.refresh(instancesWithWeight);
		return vipChooser.randomWithWeight();
	}
}
