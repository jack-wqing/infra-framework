package com.jindi.infra.benchmark.server.remote;

import com.jindi.infra.benchmark.server.model.CallTypesModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "service-topology")
public interface RemoteTopologyService {

    @PostMapping(path = "/option/callTypes")
    CallTypesModel getCallTypes();

}
