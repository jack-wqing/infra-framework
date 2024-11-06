package com.jindi.infra.governance.constant;

public enum InfraMethodEnum {
    HEALTH("HealthController"),
    MONITOR("MonitorController"),
    NACOS("NacosController"),
    GRPC("GrpcProvider"),
    ERROR("BasicErrorController"),
    SWAGGER("swagger"),
    REBOOT("RebootController");


    private String className;

    InfraMethodEnum(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
