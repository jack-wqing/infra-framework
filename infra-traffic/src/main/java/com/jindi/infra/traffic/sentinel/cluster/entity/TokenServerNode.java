package com.jindi.infra.traffic.sentinel.cluster.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TokenServerNode {

    private String ip;
    private Integer port;
    private int weight = 1;
}
