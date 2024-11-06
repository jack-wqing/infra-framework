package com.jindi.infra.grpc.client;


import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 封装简易的上下文传递工具类:客户端拦截器
 * 在发起grpc的最后一步,将上下文信息放入metadata中
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class GrpcInfraContextClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Metadata.Key<String> key = GrpcHeaderUtils.getMetadataKeyOrCreate(GrpcContextUtils.INFRA_GRPC_CONTEXT_KEY);
                if (key != null) {
                    headers.put(key, InnerJSONUtils.toJSONString(GrpcContextUtils.getThreadLocalContext()));
                }
                GrpcContextUtils.clear();
                super.start(responseListener, headers);
            }
        };
    }

}
