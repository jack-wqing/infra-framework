package com.jindi.infra.dubbo.interceptor;


import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.google.gson.reflect.TypeToken;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.MultiProtocolServerInterceptor;
import com.jindi.infra.core.constants.HeaderConsts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Activate(group = Constants.PROVIDER)
public class DubboServerMultiProtocolFilter implements Filter {


    private static AtomicBoolean hadInit = new AtomicBoolean(false);

    private static List<MultiProtocolServerInterceptor> serverInterceptors = new ArrayList<>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (hadInit.compareAndSet(false, true)) {
            initInterceptor();
        }
        Map<String, String> businessHeader = getHeader();
        String methodName = invoker.getInterface().getSimpleName()+"."+invocation.getMethodName();
        if (serverInterceptors != null && serverInterceptors.size() > 0) {
            for (MultiProtocolServerInterceptor serverInterceptor : serverInterceptors) {
                serverInterceptor.before(methodName, businessHeader);
            }
        }
        try {
            return invoker.invoke(invocation);
        } catch (Throwable e) {
            throw e;
        } finally {
            for (MultiProtocolServerInterceptor serverInterceptor : serverInterceptors) {
                serverInterceptor.after(methodName);
            }
        }
    }

    private Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        String value = RpcContext.getContext().getAttachment(HeaderConsts.INFRA_CONTEXT_HEADER_KEY);
        if (StringUtils.isNotBlank(value)) {
            headers = InnerJSONUtils.parseObject(value, new TypeToken<HashMap<String, String>>() {}.getType());
        }
        for (String key : HeaderConsts.BUSINESS_HEADER_KEY_LIST) {
            value = RpcContext.getContext().getAttachment(key);
            if (StringUtils.isNotBlank(value)) {
                headers.put(key, value);
            }
        }
        return headers;
    }

    private void initInterceptor() {
        serverInterceptors = new ArrayList<>(SpringUtil.getBeansOfType(MultiProtocolServerInterceptor.class).values());
    }
}
