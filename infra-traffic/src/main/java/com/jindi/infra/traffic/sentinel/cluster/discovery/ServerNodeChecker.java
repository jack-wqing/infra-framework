package com.jindi.infra.traffic.sentinel.cluster.discovery;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.jindi.infra.traffic.sentinel.cluster.checker.NodeChecker;
import com.jindi.infra.traffic.sentinel.cluster.entity.ClusterServerNodeEntity;
import com.jindi.infra.traffic.sentinel.cluster.entity.TokenServerNode;
import com.jindi.infra.traffic.sentinel.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 检查token server节点的变化，对客户端的IP port进行更新操作
 */
@Component
public class ServerNodeChecker implements NodeChecker {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServerNodeChecker.class);

    public static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private TokenServerDiscovery tokenServerDiscovery;

    @Autowired
    private Environment environment;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;


    private volatile TokenServerNode node;

    @EventListener
    public void readyEvent(ApplicationReadyEvent event){
        loadServerNode();
    }

    @Override
    public void loadServerNode() {
        try {
            /**
             * 允许去配置一个客户端的定请求时间，在nacos中
             */
            NamingService namingService = new NacosNamingService();
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
            properties.put(PropertyKeyConst.NAMESPACE, nacosConfigProperties.getNamespace());
            ReadableDataSource<String, ClusterClientConfig> clientConfigDs = new NacosDataSource<>(properties, nacosConfigProperties.getGroup(),
                appName(), source -> JSON.parseObject(source, new TypeReference<ClusterClientConfig>() {}));
            ClusterClientConfigManager.registerClientConfigProperty(clientConfigDs.getProperty());

            /**
             * 配置连接信息
             */
            node = tokenServerDiscovery.discovery();

            if(node != null){
                ClusterClientConfigManager.applyNewAssignConfig(new ClusterClientAssignConfig(node.getIp(), node.getPort()));
            }
            /**
             * 定时任务检测当前节点选择的token server 节点是否可用，不可用进行更新
             */
            executorService.scheduleAtFixedRate(this::updateServerNode, 1, 5, TimeUnit.SECONDS);
            /**
             * 客户端在启动的时候，集群验证会判断当前所属端的状态，需要手动手指启动，配置为止需要在后面
             */
            ClusterStateManager.registerProperty(new DynamicSentinelProperty<>(ClusterStateManager.CLUSTER_CLIENT));
        }catch (Exception e){
           LOGGER.error("ServerNodeChecker loadServerNode error!", e);
        }

    }

    @Override
    public void updateServerNode() {
        try{
            // 因为可能机器启动过程中导致集群各个节点不一致，进行重新选择查看是否更新
            ClusterServerNodeEntity healthNode = tokenServerDiscovery.discovery();
            if(healthNode == null){
                return ;
            }
            if (node != null && StringUtils.equals(healthNode.getIp(), node.getIp()) && (healthNode.getPort().equals(node.getPort()))) {
                return;
            }
            applyNewAssignConfig(healthNode);
            node = healthNode;
        }catch (Exception e){
            LOGGER.error("ServerNodeChecker updateServerNode error!", e);
        }
    }

    private void applyNewAssignConfig(TokenServerNode serverNode){
        if(serverNode == null){
            LOGGER.warn("ServerNodeChecker loadServerNode have no toke node");
            return ;
        }

    }

    private String appName(){
        String springProjectName = environment.getProperty(Constants.SPRING_APPLICATION_NAME);
        if(StringUtils.isNotBlank(springProjectName)){
            return springProjectName + Constants.CLUSTER_CLIENT_CONFIG_POSTFIX;
        }
        return AppNameUtil.getAppName() + Constants.CLUSTER_CLIENT_CONFIG_POSTFIX;
    }

}
