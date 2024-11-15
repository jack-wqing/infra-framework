package com.zspace.infra.metrics.cat.interceptor.latency;


import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.tools.RpcLatencyUtils;
import com.jindi.infra.tools.enums.RpcLatencyPeriodEnum;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class GrpcLatencyServerInterceptor implements ServerInterceptor {

    /**
     * 服务端接收请求时,通过header获取客户端携带的耗时信息,结束发送耗时后通过Context向下传递
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        String contextValue = GrpcContextUtils.get(RpcLatencyUtils.CLIENT_LATENCY_KEY);
        if (contextValue == null) {
            return next.startCall(call, headers);
        }

        contextValue = RpcLatencyUtils.endLatency(contextValue, RpcLatencyPeriodEnum.SEND_NET);
        contextValue = RpcLatencyUtils.startLatency(contextValue, RpcLatencyPeriodEnum.SERVER_BEFORE_INVOKE);
        GrpcContextUtils.put(RpcLatencyUtils.SERVER_LATENCY_KEY, contextValue);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                    @Override
                    public void sendHeaders(Metadata headers) {
                        String contextValue = GrpcContextUtils.get(RpcLatencyUtils.SERVER_LATENCY_KEY);
                        contextValue = RpcLatencyUtils.endLatency(contextValue, RpcLatencyPeriodEnum.SERVER_INVOKE);
                        contextValue = RpcLatencyUtils.startLatency(contextValue, RpcLatencyPeriodEnum.RECV_NET);
                        Metadata.Key<String> metadataKey = GrpcHeaderUtils.getMetadataKeyOrCreate(RpcLatencyUtils.SERVER_LATENCY_KEY);
                        if (metadataKey != null) {
                            headers.put(metadataKey, contextValue);
                        }
                        // TODO: 2023/4/26 回头拆到其他模块里
                        GrpcContextUtils.clearThreadLocalAndCache();
                        super.sendHeaders(headers);
                    }
                }, headers)) {
        };
    }

}
