package com.jindi.infra.traffic.sentinel.cluster.registry;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigService;
import com.google.common.collect.Lists;
import com.jindi.infra.traffic.sentinel.cluster.entity.ClientNodeEntity;
import com.jindi.infra.traffic.sentinel.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServiceRegistry implements Registry {

    public static int maxRetryTimes = 3;

    public static long INTERVAL_TIME = 7 * 24 * 60 * 60 * 1000;

    public static long INTERVAL_TIME_SECOND = 7 * 24 * 60 * 60 - 60 * 60;

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Autowired
    private Environment environment;

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @EventListener
    public void readyEvent(ApplicationReadyEvent event){
        registry();
        heartbeat();
    }

    @Override
    public void registry() {
        ConfigService configService = nacosConfigManager.getConfigService();
        do {
            try {
                log.info("namespaceSetDataId:{}, group:{}", Constants.namespaceSetDataId, Constants.SENTINEL_TOKEN_SERVER_NAME);
                String config =
                    configService.getConfig(Constants.namespaceSetDataId, Constants.SENTINEL_TOKEN_SERVER_NAME, 10000);
                List<ClientNodeEntity> ClientNodeEntities =
                    JSON.parseObject(config, new TypeReference<List<ClientNodeEntity>>() {
                    });
                log.info("namespaceSetDataId:{}, group:{}, config:{}, addr:{}, nacosConfigProperties:{}", Constants.namespaceSetDataId, Constants.SENTINEL_TOKEN_SERVER_NAME, config, nacosConfigProperties.getServerAddr(), nacosConfigProperties);
                /**
                 * 服务启动的时候，给一个清理没有使用的服务的机会
                 */
                if(ClientNodeEntities == null){
                    ClientNodeEntities = Lists.newArrayList();
                }
                List<ClientNodeEntity> validEntities = ClientNodeEntities.stream()
                    .filter(nodeEntity -> (System.currentTimeMillis() - nodeEntity.getRegisterTime()) < INTERVAL_TIME)
                    .collect(Collectors.toList());
                boolean invalid = validEntities.size() != ClientNodeEntities.size();
                boolean existed = false;
                String appName = appName();
                for (ClientNodeEntity clientNodeEntity : validEntities) {
                    if(appName.equals(clientNodeEntity.getAppName())){
                        existed = true;
                        break;
                    }
                }
                if(!existed){
                    validEntities.add(new ClientNodeEntity(appName, System.currentTimeMillis()));
                }
                if(!existed || invalid){
                    String content = JSON.toJSONString(validEntities);
                    boolean result = configService.publishConfig(Constants.namespaceSetDataId, Constants.SENTINEL_TOKEN_SERVER_NAME, content);
                    if(result){
                       break;
                    }
                }else {
                    break;
                }
            } catch (Exception e) {
                log.error("ClientNodeRegistry registry error!", e);
            }
            maxRetryTimes--;
        } while (maxRetryTimes > 0);
    }

    /**
     * 修改appname的注册时间，防止被删除
     */
    private void heartbeat(){
        executorService.scheduleAtFixedRate(this::updateAppNameTime, 1, INTERVAL_TIME_SECOND, TimeUnit.SECONDS);
    }

    private void updateAppNameTime(){
        try {
            ConfigService configService = nacosConfigManager.getConfigService();
            String config =
                configService.getConfig(Constants.namespaceSetDataId, Constants.SENTINEL_TOKEN_SERVER_NAME, 10000);
            List<ClientNodeEntity> clientNodeEntities =
                JSON.parseObject(config, new TypeReference<List<ClientNodeEntity>>() {
                });
            if(clientNodeEntities == null){
                clientNodeEntities = Lists.newArrayList();
            }
            String appName = appName();
            long curMils = System.currentTimeMillis();
            for (ClientNodeEntity clientNodeEntity : clientNodeEntities) {
                if(appName.equals(clientNodeEntity.getAppName())){
                    clientNodeEntity.setRegisterTime(curMils);
                    configService.publishConfig(Constants.namespaceSetDataId, Constants.SENTINEL_TOKEN_SERVER_NAME,  JSON.toJSONString(clientNodeEntities));
                    return ;
                }
            }
        }catch (Exception e){
            log.error("ClientNodeRegistry heartbeat error!", e);
        }
    }

    private String appName(){
        String springProjectName = environment.getProperty(Constants.SPRING_APPLICATION_NAME);
        if(StringUtils.isNotBlank(springProjectName)){
            return springProjectName;
        }
        return AppNameUtil.getAppName();
    }

}
