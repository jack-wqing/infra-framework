package com.zspace.infra.metrics.cat.interceptor.latency;


import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.tools.RpcLatencyUtils;
import com.jindi.infra.tools.enums.RpcLatencyPeriodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

@Slf4j
public class GrpcLatencyCoreServerInterceptor implements CoreRpcServerInterceptor {

    @Override
    public void before(String className, String methodName, Object... params) {
        String latencyContext = GrpcContextUtils.get(RpcLatencyUtils.SERVER_LATENCY_KEY);
        if (latencyContext == null) {
            return;
        }
        latencyContext = RpcLatencyUtils.endLatency(latencyContext, RpcLatencyPeriodEnum.SERVER_BEFORE_INVOKE);
        latencyContext = RpcLatencyUtils.startLatency(latencyContext, RpcLatencyPeriodEnum.SERVER_INVOKE);
        GrpcContextUtils.put(RpcLatencyUtils.SERVER_LATENCY_KEY, latencyContext);
    }

    @Override
    public void after(String className, String methodName, Object response, Object... params) {
        String latencyContext = GrpcContextUtils.get(RpcLatencyUtils.SERVER_LATENCY_KEY);
        if (latencyContext == null) {
            return;
        }
        latencyContext = RpcLatencyUtils.endLatency(latencyContext, RpcLatencyPeriodEnum.SERVER_INVOKE);
        latencyContext = RpcLatencyUtils.startLatency(latencyContext, RpcLatencyPeriodEnum.SERVER_AFTER_INVOKE);
        GrpcContextUtils.put(RpcLatencyUtils.SERVER_LATENCY_KEY, latencyContext);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE + 1;
    }
}
