package com.jindi.infra.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Context;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;

@Slf4j
public class ContextUtils {

    private static Cache<String, Context.Key<String>> cached = CacheBuilder.newBuilder().maximumSize(10000).build();

    public static Context.Key<String> getContextKeyOrCreate(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            return cached.get(key, () -> Context.key(key));
        } catch (ExecutionException e) {
            log.error("getContextKeyOrCreate", e);
        }
        return null;
    }

    public static String getContextValue(String key) {
        Context context = Context.current();
        if (context == null) {
            return null;
        }
        Context.Key<String> contextKey = getContextKeyOrCreate(key);
        if (contextKey == null) {
            return null;
        }
        return contextKey.get();
    }
}
