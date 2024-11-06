package com.jindi.infra.space.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCloudParam {

    /**
     * 项目名
     */
    private String project;

    /**
     * 服务名
     */
    private String service;

    /**
     * endpoint名
     */
    private String endpoint;

    /**
     * endpoint类型 SpaceCloud/Roma
     */
    private String type;
}