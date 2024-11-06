package com.jindi.infra.grpc.util;

import com.jindi.infra.common.util.InnerIpUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 目前服务端线程池被打满时，客户端打印的异常栈
 *
 * <pre>
 * io.grpc.StatusRuntimeException: CANCELLED: RST_STREAM closed stream. HTTP/2 error code: CANCEL
 * 	    at io.grpc.stub.ClientCalls.toStatusRuntimeException(ClientCalls.java:262)
 * </pre>
 */
@Slf4j
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {

    private final String threadName;

    public AbortPolicyWithReport(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED!" +
                        " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d)," +
                        " Task: %d (completed: %d)," +
                        " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s), in %s!",
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
                e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating(),
                InnerIpUtils.getCachedIP());
        RejectedExecutionException rejectedExecutionException = new RejectedExecutionException(msg);
        log.error("警告:", rejectedExecutionException);
        throw rejectedExecutionException;
    }
}
