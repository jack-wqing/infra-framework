package com.jindi.infra.dataapi.oneservice.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;
import com.jindi.infra.dataapi.oneservice.model.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OneServiceDiscoveryService {

    private static final String REGISTRATION_TIME = "registrationTime";

    /**
     * 爬坡时间：5分钟
     * 因注册时间较早，从注册到可接收请求需要持续1分钟
     */
    private static final Double UPTIME_DEGRADATION_LIMIT = 600000.0;

    @Autowired(required = false)
    private NamingService namingService;

    public Node choose(String serverName) {
        try {
            if (namingService == null) {
                // 兼容部分服务不使用nacos
                return null;
            }
            List<Instance> instances = namingService.getAllInstances(serverName);
            if (CollectionUtils.isEmpty(instances)) {
                return null;
            }
            Instance instance = selectByRandomWeight(instances);
            return new Node(instance.getIp(), instance.getPort());
        } catch (Exception e) {
            log.error("namingService get {} instances error", serverName, e);
        }
        return null;
    }

    public Instance selectByRandomWeight(List<Instance> instances) {
        List<Pair<Instance>> instancesWithWeight = new ArrayList<Pair<Instance>>();
        for (Instance instance : instances) {
            double weight = instance.getWeight();
            Map<String, String> metadata = instance.getMetadata();
            // 计算权重
            if (metadata != null && metadata.containsKey(REGISTRATION_TIME)) {
                long uptime = System.currentTimeMillis() - Long.parseLong(metadata.get(REGISTRATION_TIME));
                if (uptime > 0 && uptime < UPTIME_DEGRADATION_LIMIT) {
                    weight = instance.getWeight() * uptime / UPTIME_DEGRADATION_LIMIT;
                }
            }
            instancesWithWeight.add(new Pair<>(instance, weight));
        }
        Chooser<String, Instance> vipChooser = new Chooser<>("infra-dataapi");
        vipChooser.refresh(instancesWithWeight);
        return vipChooser.randomWithWeight();
    }
}
