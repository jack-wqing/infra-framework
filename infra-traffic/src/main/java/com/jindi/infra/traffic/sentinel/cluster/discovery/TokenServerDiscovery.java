package com.jindi.infra.traffic.sentinel.cluster.discovery;

import com.jindi.infra.traffic.sentinel.cluster.entity.TokenServerNode;

import java.util.List;

/**
 * 定发现token-server服务的节点
 */
public interface TokenServerDiscovery {

    TokenServerNode discovery();

    TokenServerNode select(List<TokenServerNode> nodeEntities);

    boolean health(TokenServerNode node);

}
