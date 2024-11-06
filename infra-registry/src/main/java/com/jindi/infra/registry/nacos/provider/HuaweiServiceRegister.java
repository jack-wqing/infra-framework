package com.jindi.infra.registry.nacos.provider;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.jindi.infra.common.constant.RegionConstant;
import com.jindi.infra.core.exception.RpcServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HuaweiServiceRegister extends BaseServiceRegister {

	private static final Integer REGISTER_TIMER_PERIOD_SECOND = 30;

	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	/**
	 * 命名服务
	 */
	private NamingService namingService;

	public HuaweiServiceRegister(NamingService namingService) {
		this.namingService = namingService;
	}

	/**
	 * 注册实例到Nacos
	 */
	public void registerInstance(String serverName, Instance instance) throws RpcServerException {
		try {
			refreshRegisterTime(instance);
			namingService.registerInstance(serverName, instance);
			log.info("实例注册nacos成功 {} => {}:{}", serverName, instance.getIp(), instance.getPort());
		} catch (Throwable e) {
			log.warn("实例注册nacos失败", e);
			retryRegisterInstance(serverName, instance);
		}
	}

	public void deregisterInstance(String serverName, Instance instance) throws NacosException {
		scheduledExecutorService.shutdown();
		namingService.deregisterInstance(serverName, instance.getIp(), instance.getPort());
	}

	/**
	 * 第一次注册失败后，重试注册
	 */
	public Boolean retryRegisterInstance(String serverName, Instance instance) throws RpcServerException {
		Throwable finalException = null;
		int retry = 2;
		while (retry > 0) {
			try {
				Thread.sleep(5000);
				namingService.registerInstance(serverName, instance);
				log.info("实例注册nacos成功 {} => {}:{}", serverName, instance.getIp(), instance.getPort());
				return true;
			} catch (Throwable e) {
				finalException = e;
				log.warn("实例注册nacos失败", e);
			}
			retry--;
		}
		throw new RpcServerException("注册nacos失败", finalException);
	}

	@Override
	protected String getRegion() {
		return RegionConstant.HUAWEI_REGION;
	}

	/**
	 * 开启定时检测实例状态
	 */
	public void checkAliveInstance(String serverName, Instance instance) {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				List<Instance> instances = namingService.getAllInstances(serverName);
				if (CollectionUtils.isEmpty(instances)) {
					registerInstance(serverName, instance);
					return;
				}
				for (Instance i : instances) {
					if (Objects.equals(i.getIp(), instance.getIp())
							&& Objects.equals(i.getPort(), instance.getPort())) {
						return;
					}
				}
				registerInstance(serverName, instance);
			} catch (Throwable e) {
				log.error("实例探活失败", e);
			}
		}, REGISTER_TIMER_PERIOD_SECOND, REGISTER_TIMER_PERIOD_SECOND, TimeUnit.SECONDS);
	}

}
