package com.jindi.infra.dataapi.oneservice.locator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.dataapi.oneservice.properties.OneServiceConfigProperties;

/**
 * 保存 appName#url -> 注册中心服务名 对应关系
 */
public class OneServiceDiscoveryLocator implements InitializingBean {

	private OneServiceConfigProperties oneServiceConfigProperties;
	/**
     * appName#url -> http://注册中心服务名/api
	 */
	private Map<String, String> oneServiceConfigServerName = new HashMap<>();

	public OneServiceDiscoveryLocator(OneServiceConfigProperties oneServiceConfigProperties) {
		this.oneServiceConfigProperties = oneServiceConfigProperties;
	}

	@Override
	public void afterPropertiesSet() {
		refresh(oneServiceConfigProperties);
	}

	public void refresh(OneServiceConfigProperties properties) {
		Map<String, String> newMap = new HashMap<>();
		Map<String, String> businessProjects = properties.getProject();
		Map<String, String> clusterServerNames = properties.getDiscovery();
		if (CollectionUtils.isEmpty(businessProjects)) {
			return;
		}
		for (Map.Entry<String, String> entry : businessProjects.entrySet()) {
			String business = entry.getKey();
			String clusterServerName = clusterServerNames.get(business);
			if (StringUtils.isBlank(clusterServerName)) {
				continue;
			}
			String projects = entry.getValue();
			if (StringUtils.isBlank(projects)) {
				continue;
			}
			Arrays.stream(projects.split(",")).forEach(project -> newMap.put(project, clusterServerName));
		}
		oneServiceConfigServerName = newMap;
	}

	public String getServerName(String key) {
		return oneServiceConfigServerName.get(key);
	}
}
