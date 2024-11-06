package com.jindi.infra.space.client;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.space.SpaceCloudException;
import com.jindi.infra.space.constant.SpaceCloudConsts;
import com.jindi.infra.space.param.SpaceCloudParam;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGraphQLHttpClient implements GraphQLHttpClient {

    private static final String URL = "url";
    private static final String TYPE = "type";
    private static final String PARAMS = "params";

    protected void logEvent(String method, Object params) {
        SpaceCloudParam spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
        if (spaceCloudParam == null) {
            throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
        }
        Cat.logEvent(String.format("%s.Method", spaceCloudParam.getType()), method, Message.SUCCESS, getMessage(spaceCloudParam, params));
    }

    protected String getUrl(SpaceCloudParam spaceCloudParam) {
        String urlPrefix = matchUrlPrefix(spaceCloudParam.getProject(), spaceCloudParam.getService(), spaceCloudParam.getEndpoint());
        return urlPrefix + "/" + spaceCloudParam.getProject() + "/services/" + spaceCloudParam.getService() + "/" + spaceCloudParam.getEndpoint();
    }

    private String getMessage(SpaceCloudParam spaceCloudParam, Object params) {
        Map<String, Object> message = new HashMap<>(3);
        String url = getUrl(spaceCloudParam);
        message.put(URL, url);
        message.put(TYPE, spaceCloudParam.getType());
        message.put(PARAMS, params);
        return InnerJSONUtils.toJSONString(message);
    }
}
