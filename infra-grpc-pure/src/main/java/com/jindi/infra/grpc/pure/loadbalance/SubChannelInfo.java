package com.jindi.infra.grpc.pure.loadbalance;

import io.grpc.LoadBalancer;
import lombok.Data;

@Data
public class SubChannelInfo {

    /**
     * grpc channel
     */
    private LoadBalancer.Subchannel subChannel;
    /**
     * 实例初始权重
     */
    private double initWeight;
    /**
     * 实例上线时间
     */
    private Long registrationTime;

    /**
     * 实例IP
     */
    private String ip;

    public SubChannelInfo(LoadBalancer.Subchannel subChannel, double initWeight, Long registrationTime, String ip) {
        this.subChannel = subChannel;
        this.initWeight = initWeight;
        this.registrationTime = registrationTime;
        this.ip = ip;
    }
}
