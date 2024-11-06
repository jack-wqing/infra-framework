package com.jindi.infra.dataapi.oneservice.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneServiceParam {

    /**
     * 项目名
     */
    private String project;

    /**
     * 目录名
     */
    private String folder;

    /**
     * api名
     */
    private String api;

    /**
     * 版本
     */
    private String version;
}