package com.jindi.infra.dataapi.spacecloud.param;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.dataapi.spacecloud.lambda.ColumnLambda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceRequestDTO implements Serializable {

    private Map<String, Object> params = new HashMap<>();

    public SpaceRequestDTO put(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public <T> SpaceRequestDTO put(ColumnLambda.SFunction<T, ?> fn, Object value) {
        params.put(ColumnLambda.getFieldName(fn), value);
        return this;
    }

    public Map<String, Object> getRomaParams() {
        if (params == null || !params.containsKey("sort")) {
            return params;
        }
        Object sorts = params.get("sort");
        if (sorts == null) {
            return params;
        }
        if (sorts instanceof Collection) {
            Collection collection = (Collection) sorts;
            if (collection.isEmpty()) {
                return params;
            }
            Map<String, Object> romaParams = new HashMap<>(params);
            romaParams.put("sort", StringUtils.join(collection, ","));
            return romaParams;
        }
        return params;
    }

    @Override
    public String toString() {
        return InnerJSONUtils.toJSONString(this);
    }
}
