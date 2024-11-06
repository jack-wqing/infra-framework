package com.jindi.infra.tools;


import com.jindi.infra.tools.enums.RpcLatencyPeriodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记录Rpc交互中各环节耗时的工具类
 * start开始,end结束(没有指定上下文的话,会默认使用ThreadLocal来保存,用完记得手动clear)
 * 存储的耗时结构如: {period}:{startTime}:{durationInMillis};
 * 例如:clientBefore:1681116870549:12;clientAfter:1681116870555:13;
 */
public class RpcLatencyUtils {

    public static final String SERVER_LATENCY_KEY = "RpcServerLatency";
    public static final String CLIENT_LATENCY_KEY = "RpcClientLatency";

    private static final String NULL_CHAR = "-";

    public static ThreadLocal<String> RPC_LATENCY_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static String startLatency(RpcLatencyPeriodEnum latencyPeriod) {
        String latencyContext = startLatency(getOrInit(), latencyPeriod);
        RPC_LATENCY_CONTEXT_THREAD_LOCAL.set(latencyContext);
        return latencyContext;
    }

    public static String startLatency(String rpcLatencyContext, RpcLatencyPeriodEnum latencyPeriod) {
        if (rpcLatencyContext.contains(latencyPeriod.getName())) {
            return rpcLatencyContext;
        }
        return start(rpcLatencyContext, latencyPeriod, System.currentTimeMillis());
    }

    public static String endLatency(RpcLatencyPeriodEnum latencyPeriod) {
        String latencyContext = endLatency(getOrInit(), latencyPeriod);
        RPC_LATENCY_CONTEXT_THREAD_LOCAL.set(latencyContext);
        return latencyContext;
    }

    public static String endLatency(String rpcLatencyContext, RpcLatencyPeriodEnum latencyPeriod) {
        return end(rpcLatencyContext, latencyPeriod);
    }

    public static String getResult() {
        return getResult(getOrInit());
    }

    public static String getResult(String contextValue) {
        List<String> result = new ArrayList<>(RpcLatencyPeriodEnum.LATENCY_PERIOD_LIST.size());
        Map<String, Latency> latencyMap = new HashMap<>();
        Long preStart = null;
        for (String str : contextValue.split(";")) {
            if (StringUtils.isBlank(str)) {
                continue;
            }
            String[] split = str.split(":");
            latencyMap.put(split[0], new Latency(NULL_CHAR.equals(split[1]) ? null : Long.parseLong(split[1]), NULL_CHAR.equals(split[2]) ? null : Long.parseLong(split[2])));
        }
        for (RpcLatencyPeriodEnum latencyPeriod : RpcLatencyPeriodEnum.LATENCY_PERIOD_LIST) {
            Latency latency = latencyMap.get(latencyPeriod.getName());
            if (latency == null) {
                continue;
            }
            if (latency.getDurationInMillis() != null) {
                result.add(latencyPeriod.getName() + ":" + latency.getDurationInMillis() + "ms");
            } else {
                if (preStart != null) {
                    result.add(latencyPeriod.getName() + ":" + (preStart - latency.getStartTime()) + "ms");
                }
            }
            if (latency.getStartTime() != null) {
                preStart = latency.getStartTime();
            }
        }
        Collections.reverse(result);
        return String.join(",", result);
    }

    private static String getOrInit() {
        String rpcLatencyContext = RPC_LATENCY_CONTEXT_THREAD_LOCAL.get();
        if (rpcLatencyContext == null) {
            rpcLatencyContext = new String();
            RPC_LATENCY_CONTEXT_THREAD_LOCAL.set(rpcLatencyContext);
        }
        return rpcLatencyContext;
    }

    public static String getContext() {
        return getOrInit();
    }

    public static void setContext(String context) {
        RPC_LATENCY_CONTEXT_THREAD_LOCAL.set(context);
    }

    public static void clear() {
        RPC_LATENCY_CONTEXT_THREAD_LOCAL.remove();
    }

    private static String start(String contextValue, RpcLatencyPeriodEnum latencyPeriodEnum, Object startTime) {
        contextValue += latencyPeriodEnum.getName() + ":" + (startTime == null ? NULL_CHAR : startTime) + ":" + NULL_CHAR + ";";
        return contextValue;
    }

    private static String end(String contextValue, RpcLatencyPeriodEnum latencyPeriodEnum) {
        int lindex = contextValue.indexOf(latencyPeriodEnum.getName());
        if (lindex == -1) {
            return contextValue;
        }
        int rindex = contextValue.indexOf(";", lindex);
        String value = contextValue.substring(lindex, rindex);
        String[] split = value.split(":");
        Long duration = System.currentTimeMillis() - Long.parseLong(split[1]);
        contextValue = contextValue.replace(value, latencyPeriodEnum.getName() + ":" + split[1] + ":" + duration);
        return contextValue;
    }

    @Data
    @AllArgsConstructor
    public static class Latency {
        private Long startTime;
        private Long durationInMillis;
    }

}
