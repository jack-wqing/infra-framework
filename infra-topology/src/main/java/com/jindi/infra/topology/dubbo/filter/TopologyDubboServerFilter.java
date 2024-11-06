package com.jindi.infra.topology.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.TopologyEsWriter;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Activate(group = Constants.PROVIDER, order = -10000)
public class TopologyDubboServerFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result;
        try {
            String header = invocation.getAttachment(TopologyConst.HEADER_CHAIN_KEY);
            TopologyHeaderUtil.copyHeaderToLocal(header);
        } catch (Throwable e) {
            TopologyEsWriter topologyEsWriter = ACUtils.getBean(TopologyEsWriter.class);
            if (topologyEsWriter != null){
                topologyEsWriter.writeException(e, "dubboServer");
            }
        }
        try {
            result = invoker.invoke(invocation);
        } catch (Throwable e) {
            throw e;
        } finally {
            TopologyHeaderUtil.cleanLocal();
        }
        return result;
    }

}
