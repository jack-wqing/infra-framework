package com.jindi.infra.grpc.pure.constant;

public class LoadBalanceConsts {
    /**
     * 爬坡阈值，120秒
     */
    public static final Double UPTIME_DEGRADATION_LIMIT = 120000.0;
    /**
     * 初始权重
     */
    public static final double WEIGHT = 1000.0;
}
