package com.jindi.infra.grpc.util;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GrpcContextUtils {

    public static final String INFRA_GRPC_CONTEXT_KEY = "infra-context";

    public static final String SERVER_REQUEST_KEY = "server-request-id";

    private static Cache<Long, Map<String, String>> cached = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000).build();

    public static Context.Key<Map<String, String>> INFRA_GRPC_CONTEXT = Context.key(INFRA_GRPC_CONTEXT_KEY);

    public static Context.Key<Long> SERVER_REQUEST_ID = Context.key(SERVER_REQUEST_KEY);

    /**
     * 用于在grpc调用之前,将上下文信息放入到threadLocal中
     */
    public static ThreadLocal<Map<String, String>> INFRA_GRPC_PRE_THREAD_LOCAL = new ThreadLocal<>();

    public static String get(String key) {
        Map<String, String> map = null;
        map = getFromLocalCached();
        if (map == null || map.get(key) == null) {
            map = getThreadLocalContext();
        }
        if (map == null || map.get(key) == null) {
            map = getFromRemoteContext();
        }
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * grpc服务端获取远端传入的对象,使用Context
     */
    private static Map<String, String> getFromRemoteContext() {
        Map<String, String> map = INFRA_GRPC_CONTEXT.get();
        if (map == null) {
            return null;
        }
        return map;
    }

    /**
     * grpc服务端内部put\get,使用本地缓存
     */
    public static Map<String, String> getFromLocalCached() {
        Long serverRequestId = SERVER_REQUEST_ID.get();
        if (serverRequestId == null) {
            return null;
        }
        return cached.getIfPresent(serverRequestId);
    }

    public static void put(String key, String value) {
        if (isGrpcServer()) {
            putLocalCached(key, value);
        }
        putThreadLocal(key, value);
    }

    public static void putThreadLocal(String key, String value) {
        Map<String, String> map = getThreadLocalContext();
        if (map == null) {
            map = initThreadLocal();
        }
        map.put(key, value);
    }

    private static void putRemoteContext(String key, String value) {
        Map<String, String> map = INFRA_GRPC_CONTEXT.get();
        if (map == null) {
            return;
        }
        map.put(key, value);
    }

    private static boolean isGrpcServer() {
        return SERVER_REQUEST_ID.get() != null;
    }

    private static void putLocalCached(String key, String value) {
        Long serverRequestId = SERVER_REQUEST_ID.get();
        if (serverRequestId == null) {
            return;
        }
        Map<String, String> map = cached.getIfPresent(serverRequestId);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(key, value);
        cached.put(serverRequestId, map);
    }

    private static Map<String, String> initThreadLocal() {
        Map<String, String> map;
        map = new HashMap<>();
        INFRA_GRPC_PRE_THREAD_LOCAL.set(map);
        return map;
    }

    public static Map<String, String> getContext() {
        Map<String, String> map = INFRA_GRPC_CONTEXT.get();
        if (map == null) {
            map = getThreadLocalContext();
            if (map == null) {
                return new HashMap<>();
            }
        }
        return map;
    }

    /**
     * grpc客户端发起请求之前put/get,使用ThreadLocal
     */
    public static Map<String, String> getThreadLocalContext() {
        return INFRA_GRPC_PRE_THREAD_LOCAL.get();
    }

    /**
     * 完成ThreadLocal清空,适用于grpc客户端发起请求之前put的对象
     */
    public static void clear() {
        INFRA_GRPC_PRE_THREAD_LOCAL.remove();
    }

    /**
     * 完成ThreadLocal和本地缓存的清空,适用于grpc服务端处理完成以后
     */
    public static void clearThreadLocalAndCache() {
        INFRA_GRPC_PRE_THREAD_LOCAL.remove();
        Long serverRequestId = SERVER_REQUEST_ID.get();
        if (serverRequestId != null) {
            cached.invalidate(serverRequestId);
        }
    }

}
