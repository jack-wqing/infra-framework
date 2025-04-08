package com.jindi.infra.grpc.server;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

// 传输的请求信息
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GrpcInfraContextServerInterceptor implements ServerInterceptor {

    private static final TypeReference<Map<String, String>> TYPE_REFERENCE = new TypeReference<Map<String, String>>(){};

    private static final AtomicLong ID = new AtomicLong(0);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current();
        String value = GrpcHeaderUtils.getHeaderValue(GrpcContextUtils.INFRA_GRPC_CONTEXT_KEY, headers);
        if (StringUtils.isNotBlank(value)) {
            ctx = ctx.withValue(GrpcContextUtils.INFRA_GRPC_CONTEXT, JSON.parseObject(value, TYPE_REFERENCE));
        }
        ctx = ctx.withValue(GrpcContextUtils.SERVER_REQUEST_ID, ID.incrementAndGet());
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
