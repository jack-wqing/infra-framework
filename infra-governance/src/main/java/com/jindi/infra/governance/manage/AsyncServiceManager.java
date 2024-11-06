package com.jindi.infra.governance.manage;

import static com.jindi.infra.common.constant.CommonConstant.PROFILE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.core.constants.EventType;
import com.jindi.infra.governance.event.TspInfraEventPublisher;

import lombok.extern.slf4j.Slf4j;

/**
 * 异步注册到服务治理平台
 *
 * @author changbo
 * @date 2021/8/29
 */
@Slf4j
public class AsyncServiceManager implements SmartLifecycle {

	@Autowired
	private Environment environment;

	@Autowired
	private TspInfraEventPublisher tspInfraEventPublisher;

	private String getApplicationName() {
		String applicationName = environment.getProperty(CommonConstant.APPLICATION_NAME);
		if (StringUtils.isBlank(applicationName)) {
			log.warn("未定义属性 {} 启动失败", CommonConstant.APPLICATION_NAME);
			System.exit(0);
		}
		return applicationName;
	}

	@Override
	public void start() {
		String profile = environment.getProperty(PROFILE);
		if ("pfaster".equals(profile)) {
			return;
		}
		Thread thread =
				new Thread(
						() -> {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							String applicationName = getApplicationName();
							String group = environment.getProperty(CommonConstant.GROUP);
							if(StringUtils.isBlank(group)) {
								group = CommonConstant.DEFAULT_GROUP;
							}
							Integer rpcPort = Integer.parseInt(
									environment.getProperty(
											CommonConstant.RPC_SERVER_PORT, CommonConstant.DEFAULT_RPC_SERVER_PORT));
							tspInfraEventPublisher.callTsp(EventType.SERVICE_REGISTER_TSP, applicationName, group, rpcPort);
							if (StringUtils.isBlank(group)) {
								log.warn("未定义属性 {} 服务默认注册到UNKNOWN节点下", CommonConstant.GROUP);
							} else {
								log.info("register tsp success, group: {}", group);
							}
							tspInfraEventPublisher.callTsp(EventType.SERVICE_INSTANCE_LANE_TAGS_SYNC, applicationName, group, rpcPort);
						});
		thread.start();
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public int getPhase() {
		return Ordered.LOWEST_PRECEDENCE - 100;
	}
}
