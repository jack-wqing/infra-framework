package com.jindi.infra.dataapi.oneservice.locator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.dataapi.oneservice.properties.OneServiceConfigProperties;

/**
 * 保存 appName#url -> 访问地址 对应关系
 */
public class OneServiceUrlLocator implements InitializingBean {

	private OneServiceConfigProperties oneServiceConfigProperties;
	/**
	 *  appName#url -> http://oneservice/api
	 */
	private Map<String, String> oneServiceConfigUrl = new HashMap<>();

	public OneServiceUrlLocator(OneServiceConfigProperties oneServiceConfigProperties) {
		this.oneServiceConfigProperties = oneServiceConfigProperties;
	}

	@Override
	public void afterPropertiesSet() {
		refresh(oneServiceConfigProperties);
	}

	public void refresh(OneServiceConfigProperties properties) {
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
			if (StringUtils.isBlank(projects)) {
				continue;
			}
			Arrays.stream(projects.split(",")).forEach(project -> newMap.put(project, url));
		}
		oneServiceConfigUrl = newMap;
	}

	public String getUrl(String key) {
		return oneServiceConfigUrl.get(key);
	}

}
