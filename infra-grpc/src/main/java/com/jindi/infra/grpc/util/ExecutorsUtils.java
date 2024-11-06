package com.jindi.infra.grpc.util;

import com.jindi.infra.tools.thread.EnhanceThreadPoolExecutor;
import io.grpc.internal.GrpcUtil;

import java.util.Objects;
import java.util.concurrent.*;

public class ExecutorsUtils {

    public static final String FIXED = "fixed";
    public static final String CACHED = "cached";

    /**
     * @param type
     * @param threads
     * @param name
     * @return
     */
    public static ExecutorService newThreadPool(String type, Integer threads, String name) {
        ThreadFactory threadFactory = GrpcUtil.getThreadFactory(name + "-%d", true);
        if (Objects.equals(type, FIXED)) {
            return new EnhanceThreadPoolExecutor(name, threads, threads,
                    0L, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(),
                    threadFactory,
                    new AbortPolicyWithReport(name));
        }
        return new EnhanceThreadPoolExecutor(name, 0, threads,
                60 * 1000, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                threadFactory, new AbortPolicyWithReport(name));
    }

    public static ExecutorService newQueueThreadPool(String type, Integer threads, String name) {
        ThreadFactory threadFactory = GrpcUtil.getThreadFactory(name + "-%d", true);
        if (Objects.equals(type, FIXED)) {
            return new EnhanceThreadPoolExecutor(name, threads, threads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    threadFactory, new AbortPolicyWithReport(name));
        }
        return new EnhanceThreadPoolExecutor(name, 0, threads,
                60 * 1000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory, new AbortPolicyWithReport(name));
    }

}
