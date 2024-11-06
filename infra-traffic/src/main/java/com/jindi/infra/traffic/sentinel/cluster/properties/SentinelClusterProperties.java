package com.jindi.infra.traffic.sentinel.cluster.properties;

import com.jindi.infra.traffic.sentinel.constant.SentinelConsts;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SentinelConsts.INFRA_SENTINEL_CLUSTER)
public class SentinelClusterProperties {

    private String server;
    private int port;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
