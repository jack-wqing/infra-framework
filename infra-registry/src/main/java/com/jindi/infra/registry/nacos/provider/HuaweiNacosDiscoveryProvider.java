package com.jindi.infra.registry.nacos.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.constant.RegionConstant;
import com.jindi.infra.core.constants.LaneTagThreadLocal;
import com.jindi.infra.grpc.extension.DiscoveryProvider;
import com.jindi.infra.grpc.extension.Node;
import com.jindi.infra.registry.nacos.properties.NacosProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * 服务发现
 */
@Slf4j
public class HuaweiNacosDiscoveryProvider implements DiscoveryProvider, EnvironmentAware {

	private final NacosProperties nacosProperties;
	private Environment environment;
	private NacosLoadBalancer nacosLoadBalancer;

	/**
	 * 命名服务
	 */
	private NamingService namingService;

	private HuaweiServiceRegister serviceRegister;

	public HuaweiNacosDiscoveryProvider(NacosProperties nacosProperties, NamingService namingService,
			NacosLoadBalancer nacosLoadBalancer, HuaweiServiceRegister serviceRegister) {
		this.nacosProperties = nacosProperties;
		this.namingService = namingService;
		this.nacosLoadBalancer = nacosLoadBalancer;
		this.serviceRegister = serviceRegister;
	}

	@Override
	public void register() throws Exception {
		if (!nacosProperties.isAutoRegister()) {
			log.info("自动注册关闭");
			return;
		}
		String serverName = environment.getProperty(CommonConstant.APPLICATION_NAME);
		Instance instance = serviceRegister.createInstance();
		serviceRegister.registerInstance(serverName, instance);
		serviceRegister.checkAliveInstance(serverName, instance);
	}

	@Override
	public void unregister() throws Exception {
		if (!nacosProperties.isAutoRegister()) {
			return;
		}
		String serverName = environment.getProperty(CommonConstant.APPLICATION_NAME);
		Instance instance = serviceRegister.createInstance();
		serviceRegister.deregisterInstance(serverName, instance);
		log.info("从nacos上注销服务");
	}

	/**
	 * 获取节点;tip：根据一定的规则，从节点列表选出
	 *
	 * @param serverName
	 *            应用名
	 * @return 节点
	 */
	public Node chooseServer(String serverName) {
		try {
			List<Instance> instances = namingService.getAllInstances(serverName);
			if (CollectionUtils.isEmpty(instances)) {
				return null;
			}
			String tag = LaneTagThreadLocal.getLaneTag();
			Node node = nacosLoadBalancer.choose(tag, serverName, instances);
			if (node == null) {
				log.error("serverName: {} 不能获取有效的实例；找不到健康的实例或者不存在注册的实例", serverName);
				return null;
			}
			return node;
		} catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
			log.error("serverName: {} 不能获取有效的实例；找不到健康的实例或者不存在注册的实例", serverName);
		} catch (IllegalStateException illegalStateException) {
			log.error("serverName: {} 从nacos获取实例 异常 {}", serverName, illegalStateException.getMessage());
		} catch (Throwable e) {
			log.error(String.format("serverName = %s 从nacos获取实例", serverName), e);
		}
		return null;
	}

	@Override
	public String getRegion() {
		return RegionConstant.HUAWEI_REGION;
	}

	/**
	 * 摘掉实例
	 *
	 * @param serverName
	 * @param ip
	 * @param port
	 * @return
	 */
	public Boolean disableInstance(String serverName, String ip, Integer port) {
		return namingService.disableInstance(serverName, ip, port);
	}

	/**
	 * 添加实例
	 *
	 * @param serverName
	 * @param ip
	 * @param port
	 * @return
	 */
	public Boolean enableInstance(String serverName, String ip, Integer port) {
		return namingService.enableInstance(serverName, ip, port);
	}

	/**
	 * 获取服务的所有实例
	 *
	 * @param serverName
	 * @return
	 */
	public List<Instance> getAllInstances(String serverName) {
		try {
			return namingService.getAllInstances(serverName);
		} catch (NacosException e) {
			log.error("", e);
		}
		return Collections.emptyList();
	}

	@Override
	public List<Node> getAllNodes(String serverName) {
		try {
			List<Instance> instances = namingService.getAllInstances(serverName);
			if (CollectionUtils.isEmpty(instances)) {
				return Collections.emptyList();
			}
			List<Node> nodes = new ArrayList<>(instances.size());
			for (Instance instance : instances) {
				// 判断实例运行状态
				if (!instance.isEnabled() || !instance.isHealthy()) {
					continue;
				}
				nodes.add(new Node(instance.getIp(), instance.getPort()));
			}
			return nodes;
		} catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
			log.error("serverName: {} 不能获取有效的实例；找不到健康的实例或者不存在注册的实例", serverName);
		} catch (IllegalStateException illegalStateException) {
			log.error("serverName: {} 从nacos获取实例 异常 {}", serverName, illegalStateException.getMessage());
		} catch (Throwable e) {
			log.error(String.format("serverName = %s 从nacos获取实例", serverName), e);
		}
		return Collections.emptyList();
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
