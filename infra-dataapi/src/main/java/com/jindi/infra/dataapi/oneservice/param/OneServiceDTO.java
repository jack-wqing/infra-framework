package com.jindi.infra.dataapi.oneservice.param;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneServiceDTO implements Serializable {

    private Map<String, Object> params = new HashMap<>();

    public OneServiceDTO put(String key, Object value) {
        params.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return InnerJSONUtils.toJSONString(this);
    }
}
