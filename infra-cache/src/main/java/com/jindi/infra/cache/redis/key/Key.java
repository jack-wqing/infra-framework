package com.jindi.infra.cache.redis.key;



import com.jindi.infra.cache.redis.exception.TycRedisException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Key {

    private final String prefix;

    private final List<String> keys;

    private final List<Object> values;

    private final String template;

    protected Key(String prefix, String[] keys, Object[] values, String template) {
        validate(keys, values);
        this.prefix = prefix;
        this.keys = toList(keys);
        this.values = toList(values);
        this.template = template;
    }

    private void validate(String[] keys, Object[] values) {
        if (keys == null && values == null) {
            return;
        }
        if (keys == null || values == null || keys.length != values.length) {
            throw new TycRedisException("key和value数量不一致");
        }
    }

    public String getTemplate() {
        return template;
    }

    public String getKey() {
        StringBuilder stringBuilder = new StringBuilder(prefix);
        Integer i = 0;
        while (i < values.size()) {
            stringBuilder.append("_").append(values.get(i++));
        }
        return stringBuilder.toString();
    }

    private <T> List<T> toList(T[] array) {
        if (array == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(array);
    }
}
