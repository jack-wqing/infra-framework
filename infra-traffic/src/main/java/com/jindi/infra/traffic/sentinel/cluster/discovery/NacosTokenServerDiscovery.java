package com.jindi.infra.traffic.sentinel.cluster.discovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.jindi.infra.traffic.sentinel.cluster.entity.TokenServerNode;
import com.jindi.infra.traffic.sentinel.cluster.selector.Selector;
import com.jindi.infra.traffic.sentinel.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NacosTokenServerDiscovery implements TokenServerDiscovery {

    @Autowired
    private Selector<TokenServerNode> selector;

    @Autowired
    @Qualifier("huaweiNamingService")
    private NamingService namingService;

    @Override
    public TokenServerNode discovery() {
        try {
            List<Instance> allInstances = namingService.getAllInstances(Constants.SENTINEL_TOKEN_SERVER_NAME, Constants.DEFAULT_GROUP);
            if(CollectionUtils.isEmpty(allInstances)){
                return null;
            }
            List<TokenServerNode> nodes = getTokenServerNodes(allInstances);
            return select(nodes);
        } catch (NacosException e) {
            log.error("NacosTokenServerDiscovery discovery error!", e);
        }
        return null;
    }

    private List<TokenServerNode> getTokenServerNodes(List<Instance> allInstances) {
        return allInstances.stream()
            .filter(instance -> StringUtils.isNotBlank(instance.getMetadata().get(Constants.SENTINEL_CLUSTER_TRANSPORT_PORT)))
            .map(instance -> {
                TokenServerNode node = new TokenServerNode();
                node.setIp(instance.getIp());
                node.setPort(Integer.valueOf(instance.getMetadata().get(Constants.SENTINEL_CLUSTER_TRANSPORT_PORT)));
                node.setWeight(new Double(instance.getWeight()).intValue());
            return node;
        }).collect(Collectors.toList());
    }

    @Override
    public TokenServerNode select(List<TokenServerNode> nodeEntities) {
        return selector.select(nodeEntities);
    }

    @Override
    public boolean health(TokenServerNode node) {
        try {
            List<Instance> instances = namingService
                .getAllInstances(Constants.SENTINEL_TOKEN_SERVER_NAME, Constants.DEFAULT_GROUP);
            for (Instance instance : instances) {
                if(StringUtils.equals(instance.getIp(), node.getIp()) && (instance.getPort() == node.getPort())){
                    return true;
                }
            }
        } catch (NacosException e) {
            log.error("NacosTokenServerDiscovery health error!", e);
        }
        return false;
    }

}
