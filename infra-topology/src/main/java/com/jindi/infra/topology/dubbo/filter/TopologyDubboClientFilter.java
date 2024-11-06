package com.jindi.infra.topology.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.TopologyEsWriter;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import com.site.lookup.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import com.jindi.infra.topology.model.ServiceCall;

import java.util.Map;


@Slf4j
@Activate(group = Constants.CONSUMER)
public class TopologyDubboClientFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result;
        try {
            ServiceCall serviceCall = ACUtils.getBean(ServiceCall.class);
            URL url = RpcContext.getContext().getUrl();
            String serverName = getServerName(url);
            String path = invocation.getMethodName();
            if (serviceCall != null && serviceCall.needProcess(serverName, path)) {
                serviceCall.pushDubboCall(serverName, path);
                String currentChain = TopologyHeaderUtil.getCurrentChain(serviceCall.getClientName(), serverName, path);
                serviceCall.pushChain(currentChain);
                Map<String, String> attachments = invocation.getAttachments();
                attachments.put(TopologyConst.HEADER_CHAIN_KEY, currentChain);
            }

        } catch (Throwable e) {
            TopologyEsWriter topologyEsWriter = ACUtils.getBean(TopologyEsWriter.class);
            if (topologyEsWriter != null){
                topologyEsWriter.writeException(e, "dubboClient");
            }
        }
        try {
            result = invoker.invoke(invocation);
        } catch (Throwable e) {
            throw e;
        }
        return result;
    }

    private String getServerName(URL url) {
        String appName = url.getParameter("serverApplicationName");
        if (StringUtils.isEmpty(appName)) {
            String interfaceName = url.getParameter("interface");
            appName = interfaceName.substring(0, interfaceName.lastIndexOf(46));
        }
        return appName;
    }

}
