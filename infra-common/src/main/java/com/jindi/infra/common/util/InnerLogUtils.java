package com.jindi.infra.common.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InnerLogUtils {

    public static void printDebugLog(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    public static void printDebugLog(String message, Object... obj) {
        if (log.isDebugEnabled()) {
            log.debug(message, obj);
        }
    }

    public static void printDebugException(String message, Exception e) {
        if (log.isDebugEnabled()) {
            log.debug(message, e);
        }
    }

    public static void printDebugException(String message, Object obj, Exception e) {
        if (log.isDebugEnabled()) {
            log.debug(message, obj, e);
        }
    }
}
