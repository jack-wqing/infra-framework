package com.jindi.infra.trace.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.dianping.cat.Cat;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.dubbo.context.DubboTraceContext;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.utils.SpringBeanUtils;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.jindi.infra.trace.utils.TraceUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Activate(group = Constants.PROVIDER, order = -10000)
public class DubboServerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DubboTraceContext dubboTraceContext = SpringBeanUtils.getBean(DubboTraceContext.class);
        TraceContext traceContext = SpringBeanUtils.getBean(TraceContext.class);
        if (Objects.isNull(dubboTraceContext) || Objects.isNull(traceContext)) {
            return invoker.invoke(invocation);
        }
        Span span = dubboTraceContext.buildServerTraceSpan(invocation, invoker, RpcContext.getContext().getAttachment("dubboApplication"));
        Result result;
        try {
            result = invoker.invoke(invocation);
            TraceUtil.tagCatMessageId(span);
        } catch (Throwable e) {
            TraceUtil.tag(span, TagsConsts.ERROR, e.getMessage());
            throw e;
        } finally {
            traceContext.writeSpan(span);
            TraceMDCUtil.clean();
        }
        return result;
    }
}

