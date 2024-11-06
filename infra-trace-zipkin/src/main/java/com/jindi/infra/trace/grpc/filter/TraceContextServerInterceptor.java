package com.jindi.infra.trace.grpc.filter;

import com.jindi.infra.common.constant.GrpcContextConstant;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.trace.grpc.context.GrpcTraceContext;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TraceContextServerInterceptor implements ServerInterceptor {

    @Resource
    private GrpcTraceContext grpcTraceContext;

    @Value("${grpc.filter.full.name:infra.TycExtend/ping,infra.TycExtend}")
    private HashSet<String> filterFullNames;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        MethodDescriptor<ReqT, RespT> method = call.getMethodDescriptor();
        if (needIgnore(method.getFullMethodName())) {
            return next.startCall(call, headers);
        }
        Context ctx = Context.current();
        TracePropagation tracePropagation = grpcTraceContext.buildTracePropagation(headers);
        Span span = grpcTraceContext.buildServerTraceContext(tracePropagation, method, headers);
        ctx = ctx.withValue(ContextUtils.getContextKeyOrCreate(GrpcContextConstant.OPENTRACING_SPAN), InnerJSONUtils.toJSONString(span));
        ctx = ctx.withValue(ContextUtils.getContextKeyOrCreate(GrpcContextConstant.TRACE_PROPAGATION), InnerJSONUtils.toJSONString(tracePropagation));
        TraceMDCUtil.clean();
        return Contexts.interceptCall(ctx, call, headers, next);
    }

    private Boolean needIgnore(String methodName) {
        if (StringUtils.isBlank(methodName) || filterFullNames.contains(methodName)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
