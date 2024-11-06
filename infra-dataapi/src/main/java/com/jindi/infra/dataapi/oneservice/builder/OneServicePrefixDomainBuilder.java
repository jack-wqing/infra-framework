package com.jindi.infra.dataapi.oneservice.builder;

import com.jindi.infra.dataapi.oneservice.locator.OneServiceUrlLocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class OneServicePrefixDomainBuilder {

    @Value("${oneservice.config.url.default}")
    private String defaultUrlPrefix;
    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private OneServiceUrlLocator oneServiceUrlLocator;

    public String build(String invokePath) {
        String key = applicationName + "#" + invokePath;
        String urlPrefix = getUrlPrefix(key);
        if (urlPrefix != null) {
            return urlPrefix;
        }
        urlPrefix = getUrlPrefix(applicationName);
        if (urlPrefix != null) {
            return urlPrefix;
        }
        return defaultUrlPrefix;
    }

    private String getUrlPrefix(String key) {
        String urlPrefix = oneServiceUrlLocator.getUrl(key);
        if (StringUtils.isNotBlank(urlPrefix)) {
            log.debug("配置匹配URL: {}->{}", key, urlPrefix);
            return urlPrefix;
        }
        return null;
    }
}
