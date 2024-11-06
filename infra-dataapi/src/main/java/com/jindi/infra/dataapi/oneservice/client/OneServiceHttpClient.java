package com.jindi.infra.dataapi.oneservice.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.dataapi.oneservice.builder.OneServicePrefixDiscoveryBuilder;
import com.jindi.infra.dataapi.oneservice.builder.OneServicePrefixDomainBuilder;
import com.jindi.infra.dataapi.oneservice.call.RestTemplateCall;
import com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts;
import com.jindi.infra.dataapi.oneservice.param.OneServiceParam;
import com.jindi.infra.dataapi.spacecloud.OneServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneServiceHttpClient extends BaseHttpClient{

    @Value("${oneservice.config.switch:domain}")
    private String configSwitch;
    @Autowired
    private RestTemplateCall restTemplateCall;
    @Autowired
    private OneServicePrefixDomainBuilder domainBuilder;
    @Autowired
    private OneServicePrefixDiscoveryBuilder discoveryBuilder;

    /**
     * 请求url获取查询结果
     */
    public String getResult(Object body) {
        OneServiceParam param = OneServiceConsts.ONE_SERVICE_PARAM_THREAD_LOCAL.get();
        if (param == null) {
            throw new OneServiceException("OneServiceConsts.ONE_SERVICE_PARAM_THREAD_LOCAL 上下文变量不存在");
        }
        String invokePath = getInvokePath(param);
        String urlPrefix = getUrlPrefix(invokePath);
        log.debug("urlPrefix : {}, path: {}", urlPrefix, invokePath);
        logEvent(urlPrefix + invokePath, "getRaw", body);
        return restTemplateCall.call(urlPrefix, invokePath, param.getProject(), param.getFolder(), param.getApi(), param.getVersion(), InnerJSONUtils.toJSONString(body));
    }

    public String getUrlPrefix(String invokePath) {
        if(configSwitch.equals("discovery")) {
            return discoveryBuilder.build(invokePath);
        } else {
            return domainBuilder.build(invokePath);
        }
    }

    protected void logEvent(String url, String method, Object params) {
        OneServiceParam param = OneServiceConsts.ONE_SERVICE_PARAM_THREAD_LOCAL.get();
        if (param == null) {
            throw new OneServiceException("OneServiceConsts.ONE_SERVICE_PARAM_THREAD_LOCAL 上下文变量不存在");
        }
        Cat.logEvent(OneServiceConsts.ONE_SERVICE, method, Message.SUCCESS, getMessage(url, params));
    }

    public String getInvokePath(OneServiceParam param) {
        return "/" + param.getProject() + "/" + param.getFolder() + "/" + param.getApi() + "/" + param.getVersion();
    }

    private String getMessage(String url, Object params) {
        Map<String, Object> message = new HashMap<>(2);
        message.put("url", url);
        message.put("params", params);
        return InnerJSONUtils.toJSONString(message);
    }
}
