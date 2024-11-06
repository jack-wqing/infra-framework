package com.jindi.infra.tools.mdc;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Transaction;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 增强Runnable，支持MDC值在线程池传递
 *
 * @author changbo
 * @date 2021/8/9
 */
public class MDCRunnable implements Runnable {

    private static final String DEFAULT_ASYNC_TYPE = "AsyncRunnable";
    private static final String RUNNER = "runner";
    private final Map<String, String> mdcMap;
    private final Runnable task;
    private final ForkedTransaction transaction;

    private MDCRunnable(Runnable task, Map<String, String> mdcMap, String threadName) {
        this.task = task;
        this.mdcMap = mdcMap;
        this.transaction = Cat.newForkedTransaction(DEFAULT_ASYNC_TYPE, threadName);
    }

    public static Runnable wrap(Runnable task) {
        Objects.requireNonNull(task);
        return new MDCRunnable(task, MDC.getCopyOfContextMap(), RUNNER);
    }

    public static Runnable wrap(Runnable task, String threadName) {
        Objects.requireNonNull(task);
        return new MDCRunnable(task, MDC.getCopyOfContextMap(), threadName);
    }

    @Override
    public void run() {
        if (!CollectionUtils.isEmpty(mdcMap)) {
            MDC.setContextMap(mdcMap);
        }

        try {
            transaction.fork();
            task.run();
            transaction.setSuccessStatus();
        } catch (Throwable e) {
            transaction.setStatus(e);
            throw e;
        } finally {
            if (!CollectionUtils.isEmpty(mdcMap)) {
                MDC.clear();
            }
            if (transaction != null) {
                transaction.complete();
            }
        }
    }
}