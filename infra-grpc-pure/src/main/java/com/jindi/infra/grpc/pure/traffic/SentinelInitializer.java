package com.jindi.infra.grpc.pure.traffic;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.jindi.infra.grpc.pure.constant.MiddlewareConfig;

import java.util.List;
import java.util.Properties;

// Sentinel治理集成
public class SentinelInitializer {

    private static final String FLOW_RULES_FORMAT = "%s-flow-rules";
    private static final String DEGRADE_RULES_FORMAT = "%s-degrade-rules";
    private static final String PROJECT_NAME = "project.name";
    private static final String CSP_SENTINEL_DASHBOARD_SERVER = "csp.sentinel.dashboard.server";

    public static void initSentinel(MiddlewareConfig middlewareConfig, String appName) {
        System.setProperty(PROJECT_NAME, appName);
        System.setProperty(CSP_SENTINEL_DASHBOARD_SERVER, middlewareConfig.getSentinelDashboardServer());
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, middlewareConfig.getNacosServerAddress());
        properties.put(PropertyKeyConst.USERNAME, middlewareConfig.getNacosUsername());
        properties.put(PropertyKeyConst.PASSWORD, middlewareConfig.getNacosPassword());
        properties.put(PropertyKeyConst.NAMESPACE, middlewareConfig.getSentinelNamespace());
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, appName,
            String.format(FLOW_RULES_FORMAT, appName), source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(properties, appName,
            String.format(DEGRADE_RULES_FORMAT, appName), source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
        }));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
    }
}
