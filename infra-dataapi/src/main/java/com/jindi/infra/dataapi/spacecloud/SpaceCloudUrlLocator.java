package com.jindi.infra.dataapi.spacecloud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.dataapi.spacecloud.properties.SpaceCloudConfigProperties;

/**
 * 保存 project#service#endpoint -> url 对应关系
 */
public class SpaceCloudUrlLocator implements InitializingBean {

	private SpaceCloudConfigProperties spaceCloudConfigProperties;

	/**
	 * 项目配置 -> space-cloud url project#service#endpoint -> http://spacecloud/path
	 */
	private Map<String, String> scConfigUrl;

	public SpaceCloudUrlLocator(SpaceCloudConfigProperties spaceCloudConfigProperties) {
		this.spaceCloudConfigProperties = spaceCloudConfigProperties;
	}

	@Override
	public void afterPropertiesSet() {
		refresh(spaceCloudConfigProperties);
	}

	public void refresh(SpaceCloudConfigProperties properties) {
		Map<String, String> newMap = new HashMap<>();
		Map<String, String> businessProjects = properties.getProject();
		Map<String, String> businessUrl = properties.getUrl();
		if (CollectionUtils.isEmpty(businessProjects)) {
			return;
		}
		for (Map.Entry<String, String> entry : businessProjects.entrySet()) {
			String business = entry.getKey();
			String url = businessUrl.get(business);
			if (StringUtils.isBlank(url)) {
				continue;
			}
			String projects = entry.getValue();
			Arrays.stream(projects.split(",")).forEach(project -> newMap.put(project, url));
		}
		if (!CollectionUtils.isEmpty(newMap)) {
			scConfigUrl = newMap;
		}
	}

	public String getUrl(String key) {
		return scConfigUrl.get(key);
	}
}
