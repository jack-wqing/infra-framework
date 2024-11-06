package com.jindi.infra.registry.nacos.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.core.constants.LaneTagThreadLocal;
import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.extension.DiscoveryProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseServiceRegister implements EnvironmentAware {

	private Environment environment;

	public static final String REGISTRY = "registry";

	/**
	 * 开启定时检测实例状态
	 */
	abstract void checkAliveInstance(String serverName, Instance instance);

	abstract void registerInstance(String serverName, Instance instance) throws Exception;

	/**
	 * 第一次注册失败后，重试2次
	 */
	abstract Boolean retryRegisterInstance(String serverName, Instance instance) throws Exception;

	/**
	 * 所属区域
	 */
	protected abstract String getRegion();

	public Instance createInstance() {
		Instance instance = new Instance();
		Map<String, String> metadata = buildServiceMeta();
		instance.setIp(InnerIpUtils.getIP());
		int port = getPort(metadata);
		instance.setPort(port);
		// fix 修复持久节点，导致的nacos误判问题
		instance.setEphemeral(true);
		instance.setWeight(1000);
		instance.setMetadata(metadata);
		return instance;
	}

	private int getPort(Map<String, String> metadata) {
		int port;
		if (StringUtils.isNotBlank(metadata.get(DiscoveryProvider.SERVICES))) {
			port = Integer.parseInt(
					environment.getProperty(CommonConstant.RPC_SERVER_PORT, CommonConstant.DEFAULT_RPC_SERVER_PORT));
		} else {
			port = Integer.parseInt(environment.getProperty(CommonConstant.SERVER_PORT));
		}
		return port;
	}

	private Map<String, String> buildServiceMeta() {
		Map<String, String> meta = new HashMap<>();
		meta.put(DiscoveryProvider.ENV, environment.getProperty(CommonConstant.PROFILE, CommonConstant.DEFAULT_ENV));
		meta.put(DiscoveryProvider.SERVER_PORT,
				environment.getProperty(CommonConstant.SERVER_PORT, CommonConstant.DEFAULT_SERVER_PORT));
		meta.put(DiscoveryProvider.SERVICES, RpcConsts.SERVICE_INFO);
		meta.put(CommonConstant.VERSION_TAG, CommonConstant.FRAMEWORK_VERSION);
		meta.put(DiscoveryProvider.REGION, getRegion());
		// 当前为单向注册华为
		meta.put(REGISTRY, "oneway-huawei");
		String laneTag = getLaneTag();
		if (StringUtils.isNotBlank(laneTag)) {
			log.info("识别到泳道标识 {}", laneTag);
			meta.put(LaneTagThreadLocal.LANE_TAG_KEY, laneTag);
		}
		return meta;
	}

	private String getLaneTag() {
		String laneTag = System.getProperty(LaneTagThreadLocal.LANE_TAG_KEY);
		if (StringUtils.isNotBlank(laneTag)) {
			return laneTag;
		}
		return environment.getProperty(LaneTagThreadLocal.LANE_TAG_KEY);
	}

	/**
	 * 刷新实例注册时间
	 */
	protected void refreshRegisterTime(Instance instance) {
		instance.getMetadata().put(DiscoveryProvider.REGISTRATION_TIME, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
