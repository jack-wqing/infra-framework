package com.jindi.infra.dubbo.interceptor;


import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import com.jindi.infra.core.constants.HeaderConsts;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Activate(group = Constants.CONSUMER)
public class DubboClientMultiProtocolFilter implements Filter {


    private static AtomicBoolean hadInit = new AtomicBoolean(false);

    private static List<MultiProtocolClientInterceptor> clientInterceptorList = new ArrayList<>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (hadInit.compareAndSet(false, true)) {
            initInterceptor();
        }
        Map<String, String> extHeader = new HashMap<>();
        String methodName = invoker.getInterface().getSimpleName()+"."+invocation.getMethodName();
        if (clientInterceptorList != null && clientInterceptorList.size() > 0) {
            for (MultiProtocolClientInterceptor clientInterceptor : clientInterceptorList) {
                clientInterceptor.before(methodName, extHeader);
            }
        }
        if (MapUtil.isNotEmpty(extHeader)) {
            RpcContext.getContext().setAttachment(HeaderConsts.INFRA_CONTEXT_HEADER_KEY, InnerJSONUtils.toJSONString(extHeader));
        }
        try {
            return invoker.invoke(invocation);
        } catch (Throwable e) {
            throw e;
        } finally {
            for (MultiProtocolClientInterceptor clientInterceptor : clientInterceptorList) {
                clientInterceptor.after(methodName);
            }
        }
    }

    private void initInterceptor() {
         clientInterceptorList = new ArrayList<>(SpringUtil.getBeansOfType(MultiProtocolClientInterceptor.class).values());
    }
}
