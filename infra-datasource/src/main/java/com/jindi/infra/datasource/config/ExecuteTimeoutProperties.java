package com.jindi.infra.datasource.config;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.datasource.dto.SqlTimeoutDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  sql execute 超时值配置
 */
@Data
public class ExecuteTimeoutProperties {

    private Map<String, Integer> methodTimeoutConfig = new HashMap<>();

    @Value("${sql.query.execute.switch:false}")
    private Boolean executeSwitch;

    @Value("${sql.query.execute.default.timeout:2000}")
    private Integer defaultTimeout;

    @Value("${sql.query.execute.timeout:}")
    public void updateMethodTimeout(String timeout) {
        if (StringUtils.isBlank(timeout)) {
            return;
        }
        List<SqlTimeoutDTO> dtos = InnerJSONUtils.parseArray(timeout, SqlTimeoutDTO.class);
        if (CollectionUtils.isEmpty(dtos)) {
            return;
        }
        methodTimeoutConfig.clear();
        for (SqlTimeoutDTO dto : dtos) {
            methodTimeoutConfig.put(dto.getMapper() + "." + dto.getMethod(), dto.getTimeout());
        }
    }



}
