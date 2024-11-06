package com.jindi.infra.traffic.sentinel.cluster.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.jindi.infra.traffic.sentinel.cluster.properties.SentinelClusterProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SentinelClusterClientInitializer {

    public SentinelClusterClientInitializer(SentinelClusterProperties properties) {
        initClusterClient(properties);
    }

    public void initClusterClient(SentinelClusterProperties properties) {
        ClusterClientAssignConfig clusterClientAssignConfig = new ClusterClientAssignConfig();
        clusterClientAssignConfig.setServerHost(properties.getServer());
        clusterClientAssignConfig.setServerPort(properties.getPort());
        SentinelProperty<ClusterClientAssignConfig> sentinelProperty = new DynamicSentinelProperty<>(clusterClientAssignConfig);
        ClusterClientConfigManager.registerServerAssignProperty(sentinelProperty);
        ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);
        log.info("init sentinel cluster client success");
    }
}
