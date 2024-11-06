package com.jindi.infra.cache.redis.utils;


public class RedisConnectInfoUtils {

    public static String getConnectInfo(String host, Object port, Object database) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(host);
        stringBuilder.append(":").append(port);
        stringBuilder.append("(").append(database).append(")");
        return stringBuilder.toString();
    }
}
