package com.jindi.infra.job.util;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.MDC;

import com.jindi.infra.job.constant.TraceConstant;

public class JobMDCUtil {

    private static final String ZERO = "0";

    public static void createTrace() {
        String traceId = createTraceId();
        MDC.put(TraceConstant.TRACE_ID, traceId);
        MDC.put(TraceConstant.SPAN_ID, traceId);
        MDC.put(TraceConstant.SAMPLED, ZERO);
    }

    public static void cleanTrace() {
        MDC.remove(TraceConstant.TRACE_ID);
        MDC.remove(TraceConstant.SPAN_ID);
        MDC.remove(TraceConstant.SAMPLED);
    }

    private static String createTraceId() {
        return HexCodec.toLowerHex(RandomUtils.nextLong());
    }
}
