package com.jindi.infra.core.util;


import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CoreRpcServerAspect {

    private static List<CoreRpcServerInterceptor> coreRpcServerInterceptorListDesc;
    private static List<CoreRpcServerInterceptor> coreRpcServerInterceptorListAsc;

    public static void init() {
        List<CoreRpcServerInterceptor> coreRpcServerInterceptorList = ACUtils.getBeansOfType(CoreRpcServerInterceptor.class);
        init(coreRpcServerInterceptorList);
    }

    public static void init(List<CoreRpcServerInterceptor> coreRpcServerInterceptorList) {
        if (CollectionUtils.isEmpty(coreRpcServerInterceptorList)) {
            coreRpcServerInterceptorListDesc = new ArrayList<>();
            coreRpcServerInterceptorListAsc = new ArrayList<>();
            return;
        }
        coreRpcServerInterceptorListDesc = coreRpcServerInterceptorList.stream()
                .sorted(Comparator.comparing(CoreRpcServerInterceptor::getOrder).reversed()).collect(Collectors.toList());
        coreRpcServerInterceptorListAsc = coreRpcServerInterceptorList.stream()
                .sorted(Comparator.comparing(CoreRpcServerInterceptor::getOrder)).collect(Collectors.toList());
    }

    public static void before(String className, String methodName, Object... params) {
        if (coreRpcServerInterceptorListDesc == null) {
            init();
        }
        if (CollectionUtils.isEmpty(coreRpcServerInterceptorListAsc)) {
            return;
        }
        for (CoreRpcServerInterceptor coreRpcServerInterceptor : coreRpcServerInterceptorListAsc) {
            coreRpcServerInterceptor.before(className, methodName, params);
        }
    }

    public static void after(String className, String methodName, Object response, Object... params) {
        if (coreRpcServerInterceptorListDesc == null) {
            init();
        }
        if (CollectionUtils.isEmpty(coreRpcServerInterceptorListDesc)) {
            return;
        }
        for (CoreRpcServerInterceptor coreRpcServerInterceptor : coreRpcServerInterceptorListDesc) {
            coreRpcServerInterceptor.after(className, methodName, response, params);
        }
    }

}
