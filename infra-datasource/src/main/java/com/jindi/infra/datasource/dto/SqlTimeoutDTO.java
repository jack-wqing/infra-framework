package com.jindi.infra.datasource.dto;

import lombok.Data;

@Data
public class SqlTimeoutDTO {

    private String mapper;

    private String method;

    private Integer timeout;
}
