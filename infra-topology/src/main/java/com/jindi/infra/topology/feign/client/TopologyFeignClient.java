package com.jindi.infra.topology.feign.client;

import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.TopologyEsWriter;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import com.jindi.infra.topology.model.ServiceCall;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class TopologyFeignClient implements Client {

    private Client delegate;
    private ServiceCall serviceCall;
    private TopologyEsWriter topologyEsWriter;

    public TopologyFeignClient(Client delegate, ServiceCall serviceCall, TopologyEsWriter topologyEsWriter) {
        this.delegate = delegate;
        this.serviceCall = serviceCall;
        this.topologyEsWriter = topologyEsWriter;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Response response;
        Request newRequest = null;
        try {
            String serverName = request.requestTemplate().feignTarget().name();
            String host = request.requestTemplate().feignTarget().url();
            String path = request.requestTemplate().path().replace(host, "");
            if (serviceCall.needProcess(serverName, path)) {
                serviceCall.pushFeignCall(serverName, path);
                String currentChain = TopologyHeaderUtil.getCurrentChain(serviceCall.getClientName(), serverName, path);
                serviceCall.pushChain(currentChain);
                newRequest = buildNewRequest(request, currentChain);
            }
        } catch (Throwable e) {
            if (topologyEsWriter != null) {
                topologyEsWriter.writeException(e, "feignClient");
            }
        }

        try {
            if (newRequest != null) {
                response = delegate.execute(newRequest, options);
            } else {
                response = delegate.execute(request, options);
            }
        } catch (Throwable e) {
            throw e;
        }
        return response;
    }

    private Request buildNewRequest(Request request, String chainValue) {
        Map<String, Collection<String>> headers = request.headers();
        Map<String, Collection<String>> newHeaders = new HashMap<>(headers);
        newHeaders.put(TopologyConst.HEADER_CHAIN_KEY, Collections.singletonList(chainValue));
        return Request.create(request.httpMethod(), request.url(), newHeaders, request.body(), request.charset(), request.requestTemplate());
    }

}
