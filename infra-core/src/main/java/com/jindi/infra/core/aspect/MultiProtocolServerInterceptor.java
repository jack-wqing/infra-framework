package com.jindi.infra.core.aspect;


import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MultiProtocolServerInterceptor {

    default void before(String method, Map<String, String> focusHeaders) {

    }

    default void after(String method) {

    }

}
