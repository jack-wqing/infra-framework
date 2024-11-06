package com.jindi.infra.tools.thread;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class CatWrapper implements Runnable {

    private static final String DEFAULT_RUN_ASYNC_TYPE = "AsyncRunnable";
    private final Runnable runnableTask;
    private final ForkedTransaction transaction;

    private static final Set<String> IGNORE_THREAD_NAME = new HashSet<>(Lists.newArrayList("grpc-server-invoke", "grpc-client-invoke", "ping-executor"));

    private CatWrapper(Runnable runnableTask, String threadName) {
        this.runnableTask = runnableTask;
        this.transaction = Cat.newForkedTransaction(DEFAULT_RUN_ASYNC_TYPE, threadName);
    }

    public static Runnable wrap(Runnable task, String threadName) {
        Objects.requireNonNull(task);
        if (!needWrap(threadName)) {
            return task;
        }
        return new CatWrapper(task, threadName);
    }

    private static boolean needWrap(String threadName) {
        if (StringUtils.isBlank(threadName)) {
            return false;
        }
        if (IGNORE_THREAD_NAME.contains(threadName)) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            transaction.fork();
            runnableTask.run();
            transaction.setSuccessStatus();
        } catch (Throwable e) {
            transaction.setStatus(e);
            throw e;
        } finally {
            if (transaction != null) {
                transaction.complete();
            }
        }
    }
}
