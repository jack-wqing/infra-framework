package com.jindi.infra.governance.lane;

import static com.jindi.infra.governance.constant.RuleConstant.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jindi.infra.common.util.InnerRestClientUtils;
import com.jindi.infra.core.constants.LaneTagThreadLocal;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LaneRule extends AbstractLoadBalancerRule {



	@Autowired
	private ObjectProvider<NamingService> namingServiceObjectProvider;
	@Autowired
	private Environment environment;

	private Cache<String, List<Instance>> nacosInstanceCache;

	private static final String REGISTRATION_TIME = "registrationTime";
	/**
	 * 爬坡，120秒
	 */
	private static final Double UPTIME_DEGRADATION_LIMIT = 120000.0;

	@PostConstruct
	public void init() {
		try {
			nacosInstanceCache= CacheBuilder.newBuilder().maximumSize(NACOS_CACHE_MAXIMUMSIZE).expireAfterWrite(NACOS_CACHE_EXPIRE, TimeUnit.MILLISECONDS).build();
		} catch (Exception e) {
			log.debug("init nacosInstanceCache error: {}", e.getMessage());
		}
	}

	public Server choose(Object key) {
		List<Server> servers = getLoadBalancer().getReachableServers();
		if (CollectionUtils.isEmpty(servers)) {
			return null;
		}
		if (!openNacosRoute()) {
			return randomChooseServer(servers);
		}
		return choose(servers);
	}

	/**
	 * 是否开启nacos路由选择，控制项目改造流量倾斜开关
	 */
	private boolean openNacosRoute() {
		String open = environment.getProperty(NACOS_SWITCH, TRUE);
		return TRUE.equals(open);
	}

	private Server randomChooseServer(List<Server> servers) {
		return servers.get(RandomUtils.nextInt(0, servers.size()));
	}

	private Server choose(List<Server> servers) {
		if(!validNamingService()) {
			// 没有注册nacos
			return randomChooseServer(servers);
		}
		String appName = getAppName(servers);
		if (StringUtils.isBlank(appName)) {
			// 实例没有appName
			return randomChooseServer(servers);
		}

		List<Instance> allEffectNacosInstances = getAllNacosInstances(appName);
		if (CollectionUtils.isEmpty(allEffectNacosInstances)) {
			// 没有有效的nacos实例
			return randomChooseServer(servers);
		}
		// 先从nacos有效实例中过滤
		List<Server> moreServers = getMoreServers(servers, allEffectNacosInstances);
		if (CollectionUtils.isNotEmpty(moreServers)) {
			// 有多余则再从nacos全部实例中过滤，再有多余的server代表切换过程，直接返回，不执行过滤逻辑
			moreServers = filterAllNacosServers(appName, moreServers);
			if (CollectionUtils.isNotEmpty(moreServers)) {
				return randomChooseServer(servers);
			}
		}

		// 根据泳道标识获取实例列表,如果没有泳道标识或者没有对应的泳道实例，则返回所有无laneTag的实例
		List<Instance> instances = filterLaneTags(allEffectNacosInstances, appName);
		if (CollectionUtils.isEmpty(instances)) {
			// 如果有异常情况,导致过滤后的实例列表为空,兜底返回randomChooseServer
			return randomChooseServer(servers);
		}

		Instance instance = getInstanceByRandomWeight(instances);

		Server server = matchServer(servers, instance);
		if (server == null) {
			// 没有匹配ip和端口的server
			return randomChooseServer(servers);
		}
		return server;
	}

	private List<Server> filterAllNacosServers(String appName, List<Server> moreServers) {
		String nacosDomain = environment.getProperty(NACOS_DOMAIN_KEY);
		if (StringUtils.isBlank(nacosDomain)) {
			return moreServers;
		}
		if (nacosInstanceCache == null) {
			return moreServers;
		}
		try {
			List<Instance> allInstances = nacosInstanceCache.get(appName, () -> {
				String url = "http://" + nacosDomain + NACOS_URI;
				Map<String, Object> params = new HashMap<>();
				params.put("serviceName", appName.toLowerCase());
				params.put("showDisableInstance", true);
				ServiceInfo serviceInfo = InnerRestClientUtils.getForObject(url, params, ServiceInfo.class);
				if (serviceInfo != null) {
					return serviceInfo.getHosts();
				} else {
					return null;
				}
			});
			if (CollectionUtils.isEmpty(allInstances)) {
				return moreServers;
			}
			return getMoreServers(moreServers, allInstances);
		} catch (Exception e) {
			log.debug("get {} all instances, error: {}", appName, e.getMessage());
			return moreServers;
		}
	}

	public List<Server> getMoreServers(List<Server> servers, List<Instance> allNacosInstances) {
		List<Server> moreServers = new ArrayList<>();
		loop: for (Server server : servers) {
			for (Instance instance : allNacosInstances) {
				if (server.getHost().equals(instance.getIp()) &&
						Objects.equals(String.valueOf(server.getPort()), instance.getMetadata().get(SERVER_PORT_KEY))) {
					continue loop ;
				}
			}
			moreServers.add(server);
		}
		return moreServers;
	}

	/**
	 * 匹配相同ip、端口的server
	 */
	private Server matchServer(List<Server> servers, Instance instance) {
		for (Server server : servers) {
			if (server.getHost().equals(instance.getIp()) &&
					Objects.equals(String.valueOf(server.getPort()), instance.getMetadata().get(SERVER_PORT_KEY))) {
				return server;
			}
		}
		return null;
	}

	/**
	 * 过滤泳道标签实例
	 */
	private List<Instance> filterLaneTags(List<Instance> allNacosInstances, String appName) {
		String tag = LaneTagThreadLocal.getLaneTag();
		List<Instance> result = new ArrayList<>(allNacosInstances.size());
		if (StringUtils.isNotBlank(tag)) {
			for (Instance instance : allNacosInstances) {
				String tags = instance.getMetadata().get(LaneTagThreadLocal.LANE_TAG_KEY);
				if (StringUtils.isNotBlank(tags)) {
					String[] tagArr = StringUtils.split(tags, TAG_SEPARATOR_CHARS);
					if (ArrayUtils.contains(tagArr, tag)) {
						result.add(instance);
					}
				}
			}
		}

		if (StringUtils.isNotBlank(tag)) {
			if (result.isEmpty()) {
				log.debug("有泳道标识,但是feign未找到具有标识的服务端,将会尝试兜底走骨干链路, tag:{}, appName:{}", tag, appName);
			} else {
				log.debug("有泳道标识 && feign找到了具有标识的服务端, tag:{}, appName:{}, address:{}", tag, appName,
						result.stream().map(Instance::getIp).collect(Collectors.joining()));
			}
		}

		if (CollectionUtils.isEmpty(result)) {
			return allNacosInstances.stream().filter(instance -> {
				String tags = instance.getMetadata().get(LaneTagThreadLocal.LANE_TAG_KEY);
				return StringUtils.isBlank(tags);
			}).collect(Collectors.toList());
		}

		return result;
	}

	/**
	 * 注册nacos的所有实例列表
	 */
	private List<Instance> getAllNacosInstances(String appName) {
		NamingService huaweiNamingService = namingServiceObjectProvider.getIfAvailable();
		try {
			List<Instance> instances = huaweiNamingService.getAllInstances(appName.toLowerCase());
			return instances.stream().filter(instance -> instance.isEnabled() && instance.isHealthy() && instance.getWeight() > 0).collect(Collectors.toList());
		} catch (Exception e) {
			log.debug("", e);
		}
		return new ArrayList<>();
	}

	/**
	 * 是否注入nacos
	 */
	private boolean validNamingService() {
		NamingService namingService = namingServiceObjectProvider.getIfAvailable();
		if (namingService == null) {
			return false;
		}
		return true;
	}

	/**
	 * 获取服务应用名
	 */
	private String getAppName(List<Server> servers) {
		for (Server server : servers) {
			String appName = server.getMetaInfo().getAppName();
			if (StringUtils.isNotBlank(appName)) {
				return appName;
			}
		}
		return null;
	}

	private Instance getInstanceByRandomWeight(List<Instance> instances) {
		List<Pair<Instance>> instancesWithWeight = new ArrayList<Pair<Instance>>();
		for (Instance instance : instances) {
			instancesWithWeight.add(new Pair<>(instance, getWeight(instance)));
		}
		Chooser<String, Instance> chooser = new Chooser<>("unique-key");
		chooser.refresh(instancesWithWeight);
		return chooser.randomWithWeight();
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

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
	}
}
