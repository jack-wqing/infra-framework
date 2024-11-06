package com.jindi.infra.dataapi.oneservice.builder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jindi.infra.dataapi.oneservice.discovery.OneServiceDiscoveryService;
import com.jindi.infra.dataapi.oneservice.locator.OneServiceDiscoveryLocator;
import com.jindi.infra.dataapi.oneservice.model.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneServicePrefixDiscoveryBuilder {

    @Value("${oneservice.config.discovery.default}")
    private String defaultServerName;
    @Value("${oneservice.config.url.default}")
    private String defaultUrlPrefix;
    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private OneServiceDiscoveryLocator locator;
    @Autowired
    private OneServiceDiscoveryService discoveryService;

    public String build(String invokePath) {
        String key = applicationName + "#" + invokePath;
        String serverName = getServerName(key);
        if (serverName != null) {
            return getUrlPrefix(serverName);
        }
        serverName = getServerName(applicationName);
        if (serverName != null) {
            return getUrlPrefix(serverName);
        }
        return getUrlPrefix(defaultServerName);
    }

    private String getServerName(String key) {
        String serverName = locator.getServerName(key);
        if (StringUtils.isNotBlank(serverName)) {
            log.debug("配置匹配ServerName: {}->{}", key, serverName);
            return serverName;
        }
        return null;
    }

    private String getUrlPrefix(String serverName) {
        Node node = discoveryService.choose(serverName);
        if(node == null) {
            return defaultUrlPrefix;
        }
        return "http://" + node.getHost() + ":" + node.getPort() + "/api";
    }
}
