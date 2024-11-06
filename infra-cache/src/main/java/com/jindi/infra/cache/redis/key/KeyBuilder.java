package com.jindi.infra.cache.redis.key;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class KeyBuilder {

    private String prefix;

    private String[] keys;

    private String template;

    private static final Map<String, KeyBuilder> KEY_BUILDER_MAP = new HashMap<>();

    private KeyBuilder(String prefix, String template, String...keys) {
        Assert.hasText(prefix, "前缀不能为空");
        this.prefix = prefix;
        this.keys = keys;
        this.template = template;
    }

    public static KeyBuilder init(String prefix, String...keys) {
        Assert.hasText(prefix, "前缀不能为空");
        String template = getTemplate(prefix, keys);
        if (KEY_BUILDER_MAP.containsKey(template)) {
            return KEY_BUILDER_MAP.get(template);
        }
        KeyBuilder keyBuilder = new KeyBuilder(prefix, template, keys);
        KEY_BUILDER_MAP.put(template, keyBuilder);
        return keyBuilder;
    }

    public Key build(Object...values) {
        return new Key(prefix, keys, values, template);
    }

    private static String getTemplate(String prefix, String[] keys) {
        StringBuilder stringBuilder = new StringBuilder(prefix);
        Integer i = 0;
        for (;keys != null && i < keys.length; i++) {
            stringBuilder.append("_{").append(keys[i]).append("}");
        }
        return stringBuilder.toString();
    }
}
