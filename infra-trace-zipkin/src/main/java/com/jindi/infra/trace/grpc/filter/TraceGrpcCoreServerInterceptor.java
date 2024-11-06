package com.jindi.infra.trace.grpc.filter;


import com.dianping.cat.Cat;
import com.jindi.infra.common.constant.GrpcContextConstant;
import com.jindi.infra.common.param.TracePropagation;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import org.apache.commons.lang3.StringUtils;

public class TraceGrpcCoreServerInterceptor implements CoreRpcServerInterceptor {
    @Override
    public void before(String className, String methodName, Object... params) {
        putTraceInfo();
        GrpcContextUtils.put(TagsConsts.CAT_MESSAGE, Cat.getCurrentMessageId());
    }

    @Override
    public void after(String className, String methodName, Object response, Object... params) {
        TraceMDCUtil.clean();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static void putTraceInfo() {
        TracePropagation tracePropagation = getTracePropagation();
        if (tracePropagation == null) {
            return;
        }
        TraceMDCUtil.putTraceInfo(tracePropagation);
    }

    private static TracePropagation getTracePropagation() {
        String tracePropagationData = ContextUtils.getContextValue(GrpcContextConstant.TRACE_PROPAGATION);
        if (StringUtils.isBlank(tracePropagationData)) {
            return null;
        }
        return InnerJSONUtils.parseObject(tracePropagationData, TracePropagation.class);
    }
}
