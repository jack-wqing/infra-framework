package com.jindi.infra.dataapi.oneservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Set;

@Slf4j
@RestController
public class OneServiceConnectController {

    @Resource
    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    @GetMapping("/connect")
    public String connect() {
        Set<HttpRoute> routes = poolingHttpClientConnectionManager.getRoutes();
        String result = "";
        for(HttpRoute route : routes) {
            PoolStats stats = poolingHttpClientConnectionManager.getStats(route);
            result += route.getTargetHost().getHostName() + ":" + route.getTargetHost().getPort() + " " + stats.toString() + "\n";
        }
        log.info(result);
        return result;
    }
}
