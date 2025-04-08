package com.jindi.infra.grpc.server;

import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.Set;

// 过滤请求头
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HeaderContextServerInterceptor implements ServerInterceptor {

    private static final Set<String> IGNORE_KEYS = new HashSet<String>() {{
        add(GrpcContextUtils.INFRA_GRPC_CONTEXT_KEY);
    }};

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current();
        Set<String> keys = headers.keys();
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                if (IGNORE_KEYS.contains(key)) {
                    continue;
                }
                ctx = ctx.withValue(ContextUtils.getContextKeyOrCreate(key), GrpcHeaderUtils.getHeaderValue(key, headers));
            }
        }
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
