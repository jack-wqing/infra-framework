package com.jindi.infra.datasource.dto;

import lombok.Data;

@Data
public class SqlLimitDTO {

    private String mapper;

    private String method;

    private Integer limit;
}
