package com.jindi.infra.biz.context;

import static com.jindi.common.tools.constant.ContextConstant.REQUEST_CONTEXT_KEY;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jindi.common.tools.context.TycRequestContext;
import com.jindi.common.tools.util.ContextUtil;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.grpc.extension.CallInterceptor;

import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestContextGrpcCallInterceptor implements CallInterceptor {

    @Override
    public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
        TycRequestContext requestContext = ContextUtil.getRequestContext();
        String context = InnerJSONUtils.toJSONString(requestContext);
        if (StringUtils.isNotBlank(context) && !context.equals("{}")) {
            extHeaders.put(REQUEST_CONTEXT_KEY, InnerJSONUtils.toJSONString(requestContext));
        }
    }
}
