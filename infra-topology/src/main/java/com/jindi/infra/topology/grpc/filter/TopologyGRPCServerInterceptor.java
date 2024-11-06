package com.jindi.infra.topology.grpc.filter;

import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.TopologyEsWriter;
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
import com.jindi.infra.core.util.ContextUtils;

import javax.annotation.Resource;
import java.util.HashSet;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TopologyGRPCServerInterceptor implements ServerInterceptor {

    @Value("${grpc.filter.full.name:infra.TycExtend/ping,infra.TycExtend}")
    private HashSet<String> filterFullNames;
    @Resource
    private TopologyEsWriter topologyEsWriter;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        try {
            MethodDescriptor<ReqT, RespT> method = call.getMethodDescriptor();
            String headerValue = GrpcHeaderUtils.getHeaderValue(TopologyConst.HEADER_CHAIN_KEY, headers);
            if (needProcess(method.getFullMethodName()) && StringUtils.isNotBlank(headerValue)) {
                Context ctx = Context.current();
                ctx = ctx.withValue(ContextUtils.getContextKeyOrCreate(TopologyConst.HEADER_CHAIN_KEY), headerValue);
                return Contexts.interceptCall(ctx, call, headers, next);
            }
        } catch (Throwable e) {
            if (topologyEsWriter != null) {
                topologyEsWriter.writeException(e, "grpcServer");
            }
        }
        return next.startCall(call, headers);
    }

    private Boolean needProcess(String methodName) {
        if (StringUtils.isBlank(methodName) || filterFullNames.contains(methodName)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
