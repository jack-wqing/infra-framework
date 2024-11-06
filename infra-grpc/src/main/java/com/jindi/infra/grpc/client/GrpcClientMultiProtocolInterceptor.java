package com.jindi.infra.grpc.client;


import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.grpc.util.NameUtils;
import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrpcClientMultiProtocolInterceptor implements CallInterceptor {

    private final List<MultiProtocolClientInterceptor> interceptorList;

    public GrpcClientMultiProtocolInterceptor(List<MultiProtocolClientInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    @Override
    public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
        Map<String, String> headers = new HashMap<>();
        String methodName = NameUtils.getSimpleMethodName(method);
        for (MultiProtocolClientInterceptor clientInterceptor : interceptorList) {
            clientInterceptor.before(methodName, headers);
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            GrpcContextUtils.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void after(Long id, MethodDescriptor method, Throwable cause) {
        String methodName = NameUtils.getSimpleMethodName(method);
        for (MultiProtocolClientInterceptor clientInterceptor : interceptorList) {
            clientInterceptor.after(methodName);
        }
    }
}
