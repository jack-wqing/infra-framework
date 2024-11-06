package com.jindi.infra.governance.event;

import com.google.common.collect.Lists;
import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.common.util.InnerRestClientUtils;
import com.jindi.infra.core.constants.EventType;
import com.jindi.infra.core.model.InfraEvent;
import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.governance.constant.InfraMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jindi.infra.governance.constant.RuleConstant.METHOD_DELIMITER;

@Slf4j
public class TspInfraEventPublisher {

	@Value("${tsp.uri:}")
	private String tspURI;

	/**
	 * 基础组件调用tsp
	 */
	public void callTsp(EventType eventType, String applicationName, String group, Integer rpcPort) {
		try {
			InfraEvent infraEvent = new InfraEvent();
			infraEvent.setGroup(group);
			infraEvent.setAppName(applicationName);
			infraEvent.setEventType(eventType);
			infraEvent.setIp(InnerIpUtils.getIP());
			infraEvent.setPort(rpcPort);
			List<String> methods = getHttpMethods();
			infraEvent.setHttpMethods(methods);
			if (StringUtils.isNotBlank(tspURI)) {
				InnerRestClientUtils.postForObject(
						String.format("%s/register", tspURI), infraEvent, Void.class);
				log.info("{}: {} success", infraEvent.getAppName(), eventType.name());
			}

		} catch (Throwable e) {
			log.error("{} fail: {}", eventType.name(), e.getMessage());
		}
	}

	private List<String> getHttpMethods() {
		List<String> methods = Lists.newArrayList();
		RequestMappingHandlerMapping rmhp = ACUtils.getBean(RequestMappingHandlerMapping.class);
		if (rmhp == null) {
			return methods;
		}
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = rmhp.getHandlerMethods();
		for (RequestMappingInfo info : handlerMethods.keySet()) {
			HandlerMethod method = handlerMethods.get(info);
			String methodName = method.toString();
			String uri = getUri(info);
			if (StringUtils.isBlank(methodName) || StringUtils.isBlank(uri) || isInfraMethod(methodName)) {
				continue;
			}
			methods.add(methodName + METHOD_DELIMITER + uri);
		}
		return methods;
	}

	private Boolean isInfraMethod(String methodName) {
		for (InfraMethodEnum infraMethodEnum: InfraMethodEnum.values()) {
			if (methodName.contains(infraMethodEnum.getClassName())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private String getUri(RequestMappingInfo info) {
		Set<String> patterns = info.getPatternsCondition().getPatterns();
		for (String uri: patterns) {
			if (StringUtils.isNotBlank(uri)) {
				return uri;
			}
		}
		return "";
	}
}
