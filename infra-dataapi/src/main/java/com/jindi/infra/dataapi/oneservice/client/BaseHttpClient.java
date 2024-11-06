package com.jindi.infra.dataapi.oneservice.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jindi.infra.common.util.InnerJSONUtils;

public abstract class BaseHttpClient {

    private static final char ARRAY_SYMBOL = '[';

    abstract String getResult(Object body);

    /**
     * 获取实体列表，按指定List类型返回
     */
    public <T> List<T> getList(Class<T> responseType, Object body) {
        String result = getResult(body);
        if (StringUtils.isBlank(result)) {
            return Collections.emptyList();
        }
        if (result.charAt(0) == ARRAY_SYMBOL) {
            return JSON.parseArray(result, responseType);
        }
        Map<String, Object> map = InnerJSONUtils.parseMap(result);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            return JSON.parseArray(InnerJSONUtils.toJSONString(entry.getValue()), responseType);
        }
        return Collections.emptyList();
    }

    /**
     * 获取单个查询结果，按指定类型返回
     */
    public <T> T getOne(Class<T> responseType, Object body) {
        List<T> list = getList(responseType, body);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取完整的实体，按指定类型返回
     */
    public <T> T get(Class<T> responseType, Object body) {
        String result = getResult(body);
        if (result == null) {
            return null;
        }
        return InnerJSONUtils.parseObject(result, responseType);
    }


}
