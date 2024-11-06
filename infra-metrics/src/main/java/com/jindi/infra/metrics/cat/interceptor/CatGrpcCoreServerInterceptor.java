package com.jindi.infra.metrics.cat.interceptor;


import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.jindi.infra.common.constant.GrpcContextConstant;
import com.jindi.infra.common.param.TracePropagation;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.core.constants.CatContext;
import com.jindi.infra.core.constants.CatType;
import com.jindi.infra.core.util.ContextUtils;
import org.apache.commons.lang3.StringUtils;

public class CatGrpcCoreServerInterceptor implements CoreRpcServerInterceptor {

    @Override
    public void before(String className, String methodName, Object... params) {
        logRpcServer();
        logRemoteCallServer();
        logTraceId();
    }

    @Override
    public void after(String className, String methodName, Object response, Object... params) {

    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static void logRpcServer() {
        Object clientData = ContextUtils.getContextKeyOrCreate(CatType.CLIENT).get();
        Object clientIpData = ContextUtils.getContextKeyOrCreate(CatType.CLIENT_IP).get();
        if (clientData == null || clientIpData == null) {
            return;
        }
        Cat.logEvent(CatType.RPC_SERVER_APP, clientData.toString());
        Cat.logEvent(CatType.RPC_SERVER_CLIENT, clientIpData.toString());
    }

    public static void logRemoteCallServer() {
        Object data = ContextUtils.getContextKeyOrCreate(CatType.CAT_CONTEXT).get();
        if (data == null) {
            return;
        }
        CatContext catContext =  InnerJSONUtils.parseObject(data.toString(), CatContext.class);
        Cat.logRemoteCallServer(catContext);
    }

    public static void logTraceId() {
        TracePropagation tracePropagation = getTracePropagation();
        if (tracePropagation == null) {
            return;
        }
        Cat.logEvent(CatType.RPC_SERVER_TRACE, "trace", Event.SUCCESS, "traceId=" + tracePropagation.getTraceId() + "&sampled=" + tracePropagation.getSampled());
    }

    private static TracePropagation getTracePropagation() {
        String tracePropagationData = ContextUtils.getContextValue(GrpcContextConstant.TRACE_PROPAGATION);
        if (StringUtils.isBlank(tracePropagationData)) {
            return null;
        }
        return InnerJSONUtils.parseObject(tracePropagationData, TracePropagation.class);
    }
}
