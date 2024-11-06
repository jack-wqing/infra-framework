package com.jindi.infra.tools.thread;


import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class PriorityBlockingQueue<R extends Runnable> extends LinkedBlockingQueue<Runnable> {

    public PriorityBlockingQueue() {
    }

    public PriorityBlockingQueue(int capacity) {
        super(capacity);
    }

    public PriorityBlockingQueue(Collection<? extends Runnable> c) {
        super(c);
    }

    public ThreadPoolExecutor executor;

    public void setExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.executor = threadPoolExecutor;
    }

    public boolean retryOffer(Runnable runnable) {
        if (executor == null || executor.isShutdown()) {
            throw new RejectedExecutionException("taskQueue.notRunning");
        }
        return super.offer(runnable);
    }

    /**
     * offer用于将任务放入阻塞队列中, true放入成功,等待执行, false放入失败,立即创建线程执行任务
     */
    @Override
    public boolean offer(Runnable runnable) {
        if (executor == null) {
            throw new RejectedExecutionException("The task queue does not have executor!");
        }

        int currentPoolThreadSize = executor.getPoolSize();
        // have free worker. put task into queue to let the worker deal with task.
        if (executor.getActiveCount() < currentPoolThreadSize) {
            return super.offer(runnable);
        }

        /**
         * //如果发现当前线程池的数量还没有到最大的maxPoolSize,返回false;告知 ThreadPoolExecutor,插入到任务队列失败,将会创建新的线程执行任务
         */
        //if we have less threads than maximum force creation of a new thread
        if (executor.getPoolSize()< executor.getMaximumPoolSize()) {
            return false;
        }

        // currentPoolThreadSize >= max
        return super.offer(runnable);
    }

}
