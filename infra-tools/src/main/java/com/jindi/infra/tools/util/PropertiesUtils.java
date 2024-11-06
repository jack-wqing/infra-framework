package com.jindi.infra.tools.util;


import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class PropertiesUtils {

    private static PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");


    /**
     * 根据配置,获取前缀
     * @param properties
     * @param prefix
     * @return
     */
    public static Properties getPropertiesByPrefix(Properties properties, String prefix) {
        if (CollectionUtils.isEmpty(properties)) {
            return new Properties();
        }
        Properties result = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (key.startsWith(prefix)) {
                String subKey = key.substring(prefix.length());
                if (subKey.startsWith(".")) {
                    subKey = subKey.substring(1);
                }
                result.put(subKey, entry.getValue());
            }
        }
        if (CollectionUtils.isEmpty(result) && prefix.contains("-")) {
            return getPropertiesByPrefix(properties, toCamel(prefix));
        }
        return result;
    }

    /**
     * 将配置的key转换为驼峰
     * @param properties
     * @return
     */
    public static Properties convert2Camel(Properties properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return properties;
        }
        Properties result = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String after = convert2Camel(String.valueOf(entry.getKey()));
            result.put(after, entry.getValue());
        }
        return result;
    }

    private static String convert2Camel(String key) {
        String[] split = key.split("-");
        if (split.length <= 1) {
            return key;
        }
        StringBuilder stringBuilder = new StringBuilder(split[0]);
        for (int i = 1; i < split.length; i++) {
            stringBuilder.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1));
        }
        return stringBuilder.toString();
    }

    /**
     * 根据前缀,从environment获取配置
     * @param environment
     * @param prefix
     * @return
     */
    public static Properties getPropertiesByPrefix(Environment environment, String prefix) {
        return getPropertiesByPrefix(environment, prefix, true);
    }

    /**
     * 根据前缀,从environment获取配置
     * @param environment
     * @param prefix
     * @return
     */
    public static Properties getPropertiesByPrefix(Environment environment, String prefix, Boolean clearPrefix) {
        return getPropertiesByPrefix(environment, prefix, null, clearPrefix);
    }

    /**
     * 根据前缀,从environment获取配置
     * @param environment
     * @param prefix
     * @return
     */
    public static Properties getPropertiesByPrefix(Environment environment, String prefix, Map<String, String> localProps) {
        return getPropertiesByPrefix(environment, prefix, localProps, true);
    }

    /**
     * 根据前缀,从environment获取配置
     * @param environment
     * @param prefix
     * @return
     */
    public static Properties getPropertiesByPrefix(Environment environment, String prefix, Map<String, String> localProps, Boolean clearPrefix) {
        if (StringUtils.isBlank(prefix) || environment == null) {
            return null;
        }
        if (!(environment instanceof AbstractEnvironment)) {
            log.debug("environment类型出错");
            return null;
        }
        AbstractEnvironment aEnv = (AbstractEnvironment) environment;
        MutablePropertySources mutablePropertySources = aEnv.getPropertySources();
        Properties properties = new Properties();
        fillInfraPropertySource(prefix, mutablePropertySources, properties);
        fillMapPropertySource(prefix, mutablePropertySources, properties);
        fillCompositePropertySource(prefix, mutablePropertySources, properties);
        fillLocalPropertySource(prefix, localProps, properties);
        processPropertyPlaceholder(properties);
        if (clearPrefix) {
            return clearPrefix(properties, prefix);
        }
        if (CollectionUtils.isEmpty(properties) && prefix.contains("-")) {
            return getPropertiesByPrefix(environment, toCamel(prefix), localProps, clearPrefix);
        }
        return properties;
    }

    private static void fillInfraPropertySource(String prefix, MutablePropertySources mutablePropertySources, Properties properties) {
        for (PropertySource<?> propertySource : mutablePropertySources) {
            if (propertySource instanceof PropertiesPropertySource && "infra-default-config".equals(propertySource.getName())) {
                PropertiesPropertySource cps = (PropertiesPropertySource) propertySource;
                String[] keys = cps.getPropertyNames();
                for (String key : keys) {
                    if (key.startsWith(prefix)) {
                        properties.put(key, String.valueOf(cps.getProperty(key)));
                    }
                }
            }
        }
    }

    private static void fillLocalPropertySource(String prefix, Map<String, String> localProps, Properties properties) {
        if (!CollectionUtils.isEmpty(localProps)) {
            localProps.forEach((key, value) -> {
                if (key.startsWith(prefix)) {
                    properties.put(key, value);
                }
            });
        }
    }

    private static Properties clearPrefix(Properties properties, String prefix) {
        Properties result = new Properties();
        properties.forEach((key, value) -> {
            String subKey = key.toString().substring(prefix.length());
            if (subKey.startsWith(".")) {
                subKey = subKey.substring(1);
            }
            result.put(subKey, value);
        });
        return result;
    }

    private static void fillCompositePropertySource(String prefix, MutablePropertySources mutablePropertySources, Properties properties) {
        for (PropertySource<?> propertySource : mutablePropertySources) {
            if (propertySource instanceof CompositePropertySource) {
                CompositePropertySource cps = (CompositePropertySource) propertySource;
                String[] keys = cps.getPropertyNames();
                for (String key : keys) {
                    if (key.startsWith(prefix)) {
                        properties.put(key, String.valueOf(cps.getProperty(key)));
                    }
                }
            }
        }
    }

    private static void fillMapPropertySource(String prefix, MutablePropertySources mutablePropertySources, Properties properties) {
        for (PropertySource<?> propertySource : mutablePropertySources) {
            if (propertySource instanceof MapPropertySource) {
                MapPropertySource mps = (MapPropertySource) propertySource;
                Set<String> keys = mps.getSource().keySet();
                for (String key : keys) {
                    if (key.startsWith(prefix)) {
                        properties.put(key, String.valueOf(mps.getProperty(key)));
                    }
                }
            }
        }
    }

    private static void processPropertyPlaceholder(Properties properties) {
        Properties tempProperties = new Properties();
        properties.forEach((key, value) -> {
            tempProperties.put(key, placeholderHelper.replacePlaceholders(value.toString(), properties));
        });
        properties.putAll(tempProperties);
    }

    public static String getProperty(Properties properties, String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(Properties properties, String[] aliasKeys) {
        for (String key : aliasKeys) {
            String value = properties.getProperty(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    public static String getProperty(Properties properties, String[] aliasKeys, String defaultValue) {
        for (String key : aliasKeys) {
            String value = properties.getProperty(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return defaultValue;
    }

    private static boolean isPrimitive(Class clz) {
        if (clz.isPrimitive()) {
            return true;
        }
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static Properties toCamel(Properties properties) {
        Properties result = new Properties();
        properties.forEach((key, value) -> result.put(PropertiesUtils.toCamel((String)key), value));
        return result;
    }

    public static String toCamel(String str) {
        String[] split = str.split("-");
        if (split.length <= 1) {
            return str;
        }
        StringBuilder stringBuilder = new StringBuilder(split[0]);
        for (int i = 1; i < split.length; i++) {
            stringBuilder.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1));
        }
        return stringBuilder.toString();
    }

    public static void removePropertiesByPrefix(Properties properties, String prefix) {
        List<String> keyList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (String.valueOf(entry.getKey()).startsWith(prefix)) {
                keyList.add(String.valueOf(entry.getKey()));
            }
        }
        keyList.forEach(properties::remove);
    }

    public static void mergeProperties(Properties source, Properties target) {
        if (source == null) {
            return;
        }
        if (target == null) {
            target = new Properties();
        }
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            target.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    public static List<Class<?>> getClasses(String value) throws ClassNotFoundException {
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        List<Class<?>> result = new ArrayList<>();
        for (String name : value.split(",")) {
            result.add(ClassUtils.forName(name, ClassUtils.getDefaultClassLoader()));
        }
        return result;
    }

    public static String getCamelPropertyWithDefault(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
             return value;
        }
        if (key.contains("-")) {
            return properties.getProperty(toCamel(key), defaultValue);
        } else {
            return properties.getProperty(toLineCase(key), defaultValue);
        }
    }

    private static String toLineCase(String key) {
        return StrUtil.toSymbolCase(key, '-');
    }

}
