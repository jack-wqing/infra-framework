package com.jindi.infra.traffic.sentinel.cluster.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientNodeEntity {

    private String appName;

    private long registerTime;



}
