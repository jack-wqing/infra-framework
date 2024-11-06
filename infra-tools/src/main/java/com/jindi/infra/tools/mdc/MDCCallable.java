package com.jindi.infra.tools.mdc;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Transaction;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 增强Callable，支持MDC值在线程池传递
 *
 * @author changbo
 * @date 2021/8/9
 */
public class MDCCallable<T> implements Callable<T> {

    private static final String DEFAULT_ASYNC_TYPE = "AsyncCallable";
    private static final String CALLER = "caller";
    private final Map<String, String> mdcMap;
    private final Callable<T> task;
    private final ForkedTransaction transaction;

    private MDCCallable(Callable<T> task, Map<String, String> mdcMap, String threadName) {
        this.task = task;
        this.mdcMap = mdcMap;
        this.transaction = Cat.newForkedTransaction(DEFAULT_ASYNC_TYPE, threadName);
    }

    public static <T> Callable wrap(Callable<T> callable) {
        Objects.requireNonNull(callable);
        return new MDCCallable(callable, MDC.getCopyOfContextMap(), CALLER);
    }

    public static <T> Callable wrap(Callable<T> callable, String threadName) {
        Objects.requireNonNull(callable);
        return new MDCCallable(callable, MDC.getCopyOfContextMap(), threadName);
    }

    @Override
    public T call() throws Exception {
        if (!CollectionUtils.isEmpty(mdcMap)) {
            MDC.setContextMap(mdcMap);
        }
        try {
            transaction.fork();
            T result = task.call();
            transaction.setSuccessStatus();
            return result;
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
