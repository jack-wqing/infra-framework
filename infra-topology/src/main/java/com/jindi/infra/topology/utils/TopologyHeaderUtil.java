package com.jindi.infra.topology.utils;

import com.jindi.infra.topology.consts.TopologyConst;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;


public class TopologyHeaderUtil {

    public static String getPreviousChain() {
        String headerValue = MDC.get(TopologyConst.HEADER_CHAIN_KEY);
        return StringUtils.isBlank(headerValue) ? "" : headerValue;
    }

    public static String getCurrentChain(String clientName, String serverName, String path) {
        String previousValue = getPreviousChain();
        String extendValue = TopologyConst.SERVER_DELIMITER + serverName + TopologyConst.SERVER_PATH_DELIMITER + path;
        if (StringUtils.isBlank(previousValue)) {
            return clientName+extendValue;
        }
        if (maxLimit(previousValue, extendValue)) {
            return previousValue;
        }
        return previousValue + extendValue;
    }

    public static void copyHeaderToLocal(String headerValue) {
        MDC.put(TopologyConst.HEADER_CHAIN_KEY, headerValue);
    }

    public static void cleanLocal() {
        MDC.clear();
    }

    private static Boolean maxLimit(String previousValue, String extendValue) {
        String[] previousValueArray = previousValue.split(TopologyConst.SERVER_DELIMITER);
        if (previousValueArray.length >= TopologyConst.CHAIN_LENGTH) {
            return Boolean.TRUE;
        }
        int dupTimes = 0;
        for (String service: previousValueArray) {
            if (service.equals(extendValue)) {
                dupTimes += 1;
                if (dupTimes >= TopologyConst.SERVICE_DUP_TIMES) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

}
