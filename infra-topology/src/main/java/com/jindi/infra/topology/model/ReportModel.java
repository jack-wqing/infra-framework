package com.jindi.infra.topology.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReportModel {
    private String clientName;
    private Map<String, Set<String>> feignCalls;
    private Map<String, Set<String>> dubboCalls;
    private Map<String, Set<String>> grpcCalls;
    private HashSet<String> chains;

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setFeignCalls(Map<String, Set<String>> feignCalls) {
        this.feignCalls = feignCalls;
    }

    public void setGrpcCalls(Map<String, Set<String>> grpcCalls) {
        this.grpcCalls = grpcCalls;
    }

    public void setDubboCalls(Map<String, Set<String>> dubboCalls) {
        this.dubboCalls = dubboCalls;
    }

    public void setChains(HashSet<String> chains) {this.chains = chains;}

    public String getClientName() {
        return clientName;
    }

    public Map<String, Set<String>> getFeignCalls() {
        return feignCalls;
    }

    public Map<String, Set<String>> getDubboCalls() {
        return dubboCalls;
    }

    public Map<String, Set<String>> getGrpcCalls() {
        return grpcCalls;
    }

    public HashSet<String> getChains(){return chains;}
}
