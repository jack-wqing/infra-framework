package com.jindi.infra.feign.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.dianping.cat.Cat;
import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;

import static com.jindi.infra.feign.constant.FeignConsts.SERVICE_PATH_DELIMITER;


@Slf4j
public class SentinelFeignClientDecorator implements Client {

    private final Client delegate;


    public SentinelFeignClientDecorator(CatFeignClientDecorator delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Entry entry = null;
        Response response = null;
        try {
            String resourceName = getResourceName(request);
            entry = SphU.entry(resourceName, EntryType.OUT);
            response = delegate.execute(request, options);
        } catch (FlowException fe) {
            handleFlowException(fe);
            throw new IOException("sentinel", fe);
        } catch (DegradeException de) {
            handleDegradeException(de);
            throw new IOException("sentinel", de);
        } catch (BlockException e) {
            handleBlockException(e);
            throw new IOException("sentinel", e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
        return response;
    }

    private void handleFlowException(FlowException flowException) {
        FlowRule flowRule = flowException.getRule();
        log.error("调用方法{}触发主调限流", flowRule.getResource());
        Cat.logError(flowException);
    }

    private void handleDegradeException(DegradeException degradeException) {
        DegradeRule degradeRule = degradeException.getRule();
        log.error("调用方法{}触发主调熔断", degradeRule.getResource());
        Cat.logError(degradeException);
    }

    private void handleBlockException(BlockException blockException) {
        AbstractRule rule = blockException.getRule();
        log.error("调用方法{}触发sentinel拦截", rule.getResource());
        Cat.logError(blockException);
    }

    private String getResourceName(Request request) {
        String serverName = request.requestTemplate().feignTarget().name();
        String host = request.requestTemplate().feignTarget().url();
        String path = request.requestTemplate().path().replace(host, "");
        return serverName+ SERVICE_PATH_DELIMITER + path;
    }
}
