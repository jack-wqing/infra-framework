package com.jindi.infra.datasource.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jindi.infra.datasource.dto.SqlLimitDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.datasource.dto.SqlTimeoutDTO;

import lombok.Data;

/**
 * sql limit 兜底值配置
 */
@Data
public class ExecuteLimitProperties {

    private Map<String, Integer> methodLimitConfig = new HashMap<>();

    @Value("${sql.query.limit.switch:false}")
    private Boolean limitSwitch;

    @Value("${sql.query.limit.default:3000}")
    private Integer defaultLimit;

    @Value("${sql.query.limit.config:}")
    public void updateLimitConfig(String config) {
        if (StringUtils.isBlank(config)) {
            return;
        }
        List<SqlLimitDTO> dtos = InnerJSONUtils.parseArray(config, SqlLimitDTO.class);
        if (CollectionUtils.isEmpty(dtos)) {
            return;
        }
        methodLimitConfig.clear();
        for (SqlLimitDTO dto : dtos) {
            methodLimitConfig.put(dto.getMapper() + "." + dto.getMethod(), dto.getLimit());
        }
    }



}
