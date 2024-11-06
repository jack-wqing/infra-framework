package com.jindi.infra.tools.mdc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 增强线程池，支持MDC值在线程池传递
 *
 * @author changbo
 * @date 2021/8/4
 */
@Deprecated
public class MDCEnhanceExecutorService implements ExecutorService {

    private ExecutorService delegate;

    public MDCEnhanceExecutorService(ExecutorService executorService) {
        this.delegate = executorService;
    }

    public void execute(Runnable command) {
        this.delegate.execute(MDCRunnable.wrap(command));
    }

    public void shutdown() {
        this.delegate.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return this.delegate.shutdownNow();
    }

    public boolean isShutdown() {
        return this.delegate.isShutdown();
    }

    public boolean isTerminated() {
        return this.delegate.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate.submit(MDCCallable.wrap(task));
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return this.delegate.submit(MDCRunnable.wrap(task), result);
    }

    public Future<?> submit(Runnable task) {
        return this.delegate.submit(MDCRunnable.wrap(task));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallable(tasks));
    }

    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallable(tasks), timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(this.wrapCallable(tasks));
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(this.wrapCallable(tasks), timeout, unit);
    }

    private <T> Collection<? extends Callable<T>> wrapCallable(
            Collection<? extends Callable<T>> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return tasks;
        }
        List<Callable<T>> callables = new ArrayList(tasks.size());
        for (Callable<T> task : tasks) {
            if (task instanceof MDCCallable) {
                callables.add(task);
                continue;
            }
            callables.add(MDCCallable.wrap(task));
        }
        return callables;
    }
}
