package com.jindi.infra.grpc.pure.discovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.jindi.infra.grpc.pure.constant.DiscoveryConsts;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.jindi.infra.grpc.pure.constant.DiscoveryConsts.*;

@Slf4j
public class NacosNameResolver extends NameResolver {

	private static final String SERVER_PORT = "serverPort";
	private List<NamingService> namingServices;
	private URI targetUri;

	public NacosNameResolver(List<NamingService> namingServices, URI targetUri) {
		this.namingServices = namingServices;
		this.targetUri = targetUri;
	}

	@Override
	public void start(Listener2 listener) {
		String serviceName = targetUri.getAuthority();
		try {
			init(listener, serviceName);
		} catch (Throwable e) {
			log.error("", e);
		}
		try {
			for (NamingService namingService : namingServices) {
				namingService.subscribe(serviceName, event -> {
					changeHandler(listener, (NamingEvent) event);
				});
			}
		} catch (Throwable e) {
			log.error("", e);
		}
	}

	/**
	 * 创建客户端时初始化实例列表
	 *
	 * @param listener
	 * @param serviceName
	 * @throws NacosException
	 */
	private void init(Listener2 listener, String serviceName) throws NacosException {
		List<EquivalentAddressGroup> equivalentAddressGroups = new ArrayList<>();
		for (NamingService namingService : namingServices) {
			List<Instance> instances = namingService.getAllInstances(serviceName);
			for (Instance instance : instances) {
				if (!instance.isEnabled() || !instance.isHealthy()) {
					continue;
				}
				Attributes attributes = Attributes.newBuilder()
						.set(WEIGHT_ATTRIBUTES_KEY, instance.getWeight())
						.set(REGISTRATION_TIME_ATTRIBUTES_KEY, instance.getMetadata().get(REGISTRATION_TIME_KEY))
						.build();
				equivalentAddressGroups.add(new EquivalentAddressGroup(
						new InetSocketAddress(instance.getIp(), instance.getPort()), attributes));
			}
		}
		listener.onResult(ResolutionResult.newBuilder().setAddresses(equivalentAddressGroups).build());
	}

	/**
	 * 实例列表变更本地缓存
	 *
	 * @param listener
	 * @param namingEvent
	 */
	private void changeHandler(Listener2 listener, NamingEvent namingEvent) {
		List<EquivalentAddressGroup> equivalentAddressGroups = new ArrayList<>();
		for (NamingService namingService : namingServices) {
			try {
				List<Instance> instances = namingService.getAllInstances(namingEvent.getServiceName());
				if (instances == null || instances.isEmpty()) {
					continue;
				}
				for (Instance instance : instances) {
					if (!instance.isEnabled() || !instance.isHealthy()) {
						continue;
					}
					Map<String, String> metadata = instance.getMetadata();
					if (metadata != null && !metadata.isEmpty()) {
						String serverPort = metadata.get(SERVER_PORT);
						if (StringUtils.isNotBlank(serverPort)) {
							if (Objects.equals(serverPort, String.valueOf(instance.getPort()))) {
								continue;
							}
						}
					}
					Attributes attributes = Attributes.newBuilder()
							.set(WEIGHT_ATTRIBUTES_KEY, instance.getWeight())
							.set(REGISTRATION_TIME_ATTRIBUTES_KEY, instance.getMetadata().get(REGISTRATION_TIME_KEY))
							.build();
					equivalentAddressGroups.add(new EquivalentAddressGroup(
							new InetSocketAddress(instance.getIp(), instance.getPort()), attributes));
				}
			} catch (Throwable e) {
				log.error("namingService.getAllInstances {}", namingEvent.getServiceName(), e);
			}
		}
		listener.onResult(ResolutionResult.newBuilder().setAddresses(equivalentAddressGroups).build());
	}

	@Override
	public String getServiceAuthority() {
		return targetUri.getAuthority();
	}

	@Override
	public void shutdown() {
	}
}
