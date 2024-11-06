package com.jindi.infra.topology.grpc.filter;

import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.ServiceCall;
import com.jindi.infra.topology.model.TopologyEsWriter;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import io.grpc.MethodDescriptor;
import com.jindi.infra.grpc.model.CallContext;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;

public class TopologyGRPCCallInterceptor implements CallInterceptor {
    @Resource
    private ServiceCall serviceCall;
    @Resource
    private TopologyEsWriter topologyEsWriter;
    @Value("${grpc.filter.full.name:infra.TycExtend/ping,infra.TycExtend}")
    private HashSet<String> filterFullNames;

    @Override
    public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
        try {
            if (filterFullNames.contains(method.getFullMethodName())) {
                return;
            }
            CallContext callContext = CallContext.currentCallContext();
            if(callContext == null) {
                return;
            }
            String serverName = callContext.getServerName();
            String path = callContext.getMethod().getName();
            if (serviceCall.needIgnore(serverName, path)) {
                return;
            }
            serviceCall.pushGrpcCall(serverName, path);
            String currentChain = TopologyHeaderUtil.getCurrentChain(serviceCall.getClientName(), serverName, path);
            serviceCall.pushChain(currentChain);
            extHeaders.put(TopologyConst.HEADER_CHAIN_KEY, currentChain);
        } catch (Throwable e) {
            if (topologyEsWriter != null) {
                topologyEsWriter.writeException(e, "grpcClient");
            }
        }
    }

}
