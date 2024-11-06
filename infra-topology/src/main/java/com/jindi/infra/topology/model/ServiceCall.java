package com.jindi.infra.topology.model;

import com.jindi.infra.common.util.InnerEnvironmentUtils;
import com.jindi.infra.tools.util.RestClientUtils;
import com.jindi.infra.topology.consts.TopologyConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceCall {

    @Value("${spring.application.name}")
    private String clientName;
    @Autowired
    private Environment environment;
    @Resource
    private TopologyEsWriter topologyEsWriter;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private Map<String, Set<String>> feignCalls = new ConcurrentHashMap<>();
    private Map<String, Set<String>> grpcCalls = new ConcurrentHashMap<>();
    private Map<String, Set<String>> dubboCalls = new ConcurrentHashMap<>();

    private HashSet<String> chains = new HashSet<>();

    @PostConstruct
    public void init() {

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                String url = buildReportUrl();
                if (StringUtils.isNotBlank(url)) {
                    ReportModel reportModel = buildReportModel();
                    RestClientUtils.postForObject(url, reportModel, Void.class, TopologyConst.CONNECT_TIMEOUT, TopologyConst.REQUEST_TIMEOUT);
                }

            } catch (Throwable e) {
                if (topologyEsWriter != null) {
                    topologyEsWriter.writeException(e, "report");
                }
            } finally {
                dubboCalls.clear();
                grpcCalls.clear();
                feignCalls.clear();
                chains.clear();
            }
        }, TopologyConst.REPORT_INTERVAL, TopologyConst.REPORT_INTERVAL, TimeUnit.SECONDS);
    }

    public void pushFeignCall(String serverName, String path) {
        if (maxLimit(feignCalls, serverName)) {
            return;
        }
        if (!feignCalls.containsKey(serverName)) {
            feignCalls.put(serverName, new HashSet<>());
        }
        feignCalls.get(serverName).add(path);
    }

    public void pushGrpcCall(String serverName, String path) {
        if (maxLimit(grpcCalls, serverName)) {
            return;
        }
        if (!grpcCalls.containsKey(serverName)) {
            grpcCalls.put(serverName, new HashSet<>());
        }
        grpcCalls.get(serverName).add(path);

    }

    public void pushDubboCall(String serverName, String path) {
        if (maxLimit(dubboCalls, serverName)) {
            return;
        }
        if (!dubboCalls.containsKey(serverName)) {
            dubboCalls.put(serverName, new HashSet<>());
        }
        dubboCalls.get(serverName).add(path);

    }

    private ReportModel buildReportModel() {
        ReportModel reportModel = new ReportModel();
        reportModel.setClientName(clientName);
        reportModel.setGrpcCalls(grpcCalls);
        reportModel.setDubboCalls(dubboCalls);
        reportModel.setFeignCalls(feignCalls);
        reportModel.setChains(chains);
        return reportModel;

    }

    private String buildReportUrl() {
        String[] activeProfiles = environment.getActiveProfiles();
        String envName = InnerEnvironmentUtils.getEnv(environment);
        if (InnerEnvironmentUtils.isProd(activeProfiles)) {
            return String.format(TopologyConst.REPORT_URL, "");
        }
        if (InnerEnvironmentUtils.isYufa(activeProfiles)) {
            return String.format(TopologyConst.REPORT_URL, "-" + TopologyConst.YUFA_ENV);
        }
        if ("dev".equals(envName)) {
            envName = "test";
        }
        return String.format(TopologyConst.REPORT_URL, "-" + envName);
    }

    public String getClientName() {
        return clientName;
    }

    public void pushChain(String chainValue) {
        if (maxLimit(chains)) {
            return;
        }
        if (StringUtils.isNotBlank(chainValue)) {
            chains.add(chainValue);
        }

    }

    public Boolean needProcess(String serverName, String path) {
        return StringUtils.isNotBlank(serverName) && StringUtils.isNotBlank(path);
    }

    public Boolean needIgnore(String serverName, String path) {
        return StringUtils.isBlank(serverName) || StringUtils.isBlank(path);
    }

    public Boolean maxLimit(Set<String> set) {
        return set.size() >= TopologyConst.CHAIN_CAPACITY;
    }

    public Boolean maxLimit(Map<String, Set<String>> calls, String serverName) {
        if (calls.size() >= TopologyConst.SERVICE_CAPACITY) {
            return Boolean.TRUE;
        }
        Set<String> tmpSet = calls.get(serverName);
        if (tmpSet != null && tmpSet.size() > TopologyConst.SERVICE_PATH_CAPACITY) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
