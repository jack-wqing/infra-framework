package com.jindi.infra.topology.consts;

public class TopologyConst {
    public static final Long REPORT_INTERVAL = 60L;
    public static final String REPORT_URL = "http://topology%s.jindidata.com/cache/service-call";
    public static final String YUFA_ENV = "pre";
    public static final int REQUEST_TIMEOUT = 3000;
    public static final int CONNECT_TIMEOUT = 3000;

    public static final String HEADER_CHAIN_KEY = "topology-chain";
    public static final String SERVER_PATH_DELIMITER = "|";
    public static final String SERVER_DELIMITER = ",";

    public static final int SERVICE_CAPACITY = 500;
    public static final int SERVICE_PATH_CAPACITY = 500;
    public static final int CHAIN_CAPACITY = 500;
    public static final int CHAIN_LENGTH = 50;
    public static final int SERVICE_DUP_TIMES = 4;

    public static final String ES_TYPE = "topology";
}
