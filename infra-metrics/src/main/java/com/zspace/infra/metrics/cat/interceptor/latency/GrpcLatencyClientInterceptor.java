package com.zspace.infra.metrics.cat.interceptor.latency;


import com.dianping.cat.message.Transaction;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.tools.RpcLatencyUtils;
import com.jindi.infra.tools.enums.RpcLatencyPeriodEnum;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class GrpcLatencyClientInterceptor implements ClientInterceptor {

    private static final AtomicLong REQUEST_ID = new AtomicLong(0);
    private static final ThreadLocal<Long> RPC_LATENCY_REQUEST_ID_KEY = new ThreadLocal<>();
    private static Cache<Long, String> cached = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000).build();

    /**
     * 客户端发起请求时,通过AOP增加开始打打点
     **/
    @Before("execution(public * com.jindi.infra.grpc.client.CatGrpcClientHandler.newTransaction(..))")
    public void clientBefore() {
        RpcLatencyUtils.startLatency(RpcLatencyPeriodEnum.CLIENT_BEFORE);
    }

    /**
     * 客户端发起请求时,通过AOP增加开始打打点
     **/
    @Before("execution(public * com.jindi.infra.grpc.client.CatGrpcClientHandler.complete(..))")
    public void clientEnd(JoinPoint joinPoint) {
        try {
            Object arg = joinPoint.getArgs()[0];
            if (!(arg instanceof Transaction) || arg == null) {
                return;
            }
            Long requestId = RPC_LATENCY_REQUEST_ID_KEY.get();
            if (requestId == null) {
                return;
            }
            RPC_LATENCY_REQUEST_ID_KEY.remove();
            String latencyContext = cached.getIfPresent(requestId);
            if (StringUtils.isBlank(latencyContext)) {
                return;
            }
            cached.invalidate(requestId);
            latencyContext = RpcLatencyUtils.endLatency(latencyContext, RpcLatencyPeriodEnum.CLIENT_AFTER);
            ((Transaction) arg).addData(RpcLatencyUtils.getResult(latencyContext));
        } catch (Exception e) {
            log.error("记录grpc耗时出现异常", e);
        } finally {
            RpcLatencyUtils.clear();
        }
    }

    /**
     * 客户端发起请求时
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        Long requestId = REQUEST_ID.incrementAndGet();
        RPC_LATENCY_REQUEST_ID_KEY.set(requestId);
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                try {
                    RpcLatencyUtils.endLatency(RpcLatencyPeriodEnum.CLIENT_BEFORE);
                    RpcLatencyUtils.startLatency(RpcLatencyPeriodEnum.SEND_NET);
                    GrpcContextUtils.putThreadLocal(RpcLatencyUtils.CLIENT_LATENCY_KEY, RpcLatencyUtils.getContext());
                    RpcLatencyUtils.clear();
                } catch (Throwable e) {
                    log.error("插入时延出错", e);
                }
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onHeaders(Metadata headers) {
                                Metadata.Key<String> metadataKey = GrpcHeaderUtils.getMetadataKeyOrCreate(RpcLatencyUtils.SERVER_LATENCY_KEY);
                                String latencyContextValue = headers.get(metadataKey);
                                if (StringUtils.isNotBlank(latencyContextValue)) {
                                    latencyContextValue = RpcLatencyUtils.endLatency(latencyContextValue, RpcLatencyPeriodEnum.RECV_NET);
                                    latencyContextValue = RpcLatencyUtils.startLatency(latencyContextValue, RpcLatencyPeriodEnum.CLIENT_AFTER);
                                    cached.put(requestId, latencyContextValue);
                                }
                                super.onHeaders(headers);
                            }
                        }, headers);
            }

            @Override
            public void cancel(String message, Throwable cause) {
                super.cancel(message, cause);
            }
        };
    }

}
