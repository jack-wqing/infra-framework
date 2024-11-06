package com.jindi.infra.grpc.server;


import com.google.gson.reflect.TypeToken;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.core.aspect.MultiProtocolServerInterceptor;
import com.jindi.infra.core.constants.HeaderConsts;
import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrpcServerMultiProtocolInterceptor implements CoreRpcServerInterceptor {

    private final List<MultiProtocolServerInterceptor> interceptorList;

    public GrpcServerMultiProtocolInterceptor(List<MultiProtocolServerInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    @Override
    public void before(String className, String methodName, Object... params) {
        String name = className + "." + methodName;
        Map<String, String> focusHeaders = getHeaders();
        for (MultiProtocolServerInterceptor interceptor : interceptorList) {
            interceptor.before(name, focusHeaders);
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = GrpcContextUtils.getContext();;
        for (String key : HeaderConsts.BUSINESS_HEADER_KEY_LIST) {
            String value = ContextUtils.getContextValue(key);
            if (StringUtils.isNotBlank(value)) {
                headers.put(key, value);
            }
        }
        return headers;
    }

    @Override
    public void after(String className, String methodName, Object response, Object... params) {
        String name = className + "." + methodName;
        for (MultiProtocolServerInterceptor interceptor : interceptorList) {
            interceptor.after(name);
        }
    }

    @Override
    public int getOrder() {
         return 0;
    }

}
