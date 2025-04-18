package com.jindi.infra.tools.mdc;

import com.dianping.cat.Cat;

import java.util.HashMap;
import java.util.Map;

public class CatContext implements Cat.Context {

    private final Map<String, String> properties = new HashMap<>();

    @Override
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }
}