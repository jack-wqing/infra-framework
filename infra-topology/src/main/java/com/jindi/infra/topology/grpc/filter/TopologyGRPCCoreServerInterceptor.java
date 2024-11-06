package com.jindi.infra.topology.grpc.filter;

import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.TopologyEsWriter;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;

import javax.annotation.Resource;

public class TopologyGRPCCoreServerInterceptor implements CoreRpcServerInterceptor {
    @Resource
    private TopologyEsWriter topologyEsWriter;

    @Override
    public void before(String className, String methodName, Object... params) {
        try {
            String headerValue = ContextUtils.getContextValue(TopologyConst.HEADER_CHAIN_KEY);
            TopologyHeaderUtil.copyHeaderToLocal(headerValue);
        } catch (Throwable e) {
            if (topologyEsWriter != null) {
                topologyEsWriter.writeException(e, "grpcCoreServer");
            }
        }
    }

    @Override
    public void after(String className, String methodName, Object response, Object... params) {
        TopologyHeaderUtil.cleanLocal();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
