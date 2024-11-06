package com.jindi.infra.tools.thread;


import com.dianping.cat.Cat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EnhanceThreadPoolExecutor extends ThreadPoolExecutor {

    protected final String poolName;

    //最短执行时间
    private long minCostTime;
    //最长执行时间
    private long maxCostTime;
    //总的耗时
    private final AtomicLong totalCostTime = new AtomicLong(0L);
    //线程池标识
    private final Integer threadPoolTagId;
    //线程池标识标记
    private static final AtomicInteger threadPoolTag = new AtomicInteger(0);

    private final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    /**
     * 拒绝数量.
     */
    private final AtomicInteger rejectCount = new AtomicInteger(0);

    /**
     * 拒绝策略名称.
     */
    private String rejectHandlerName;

    private boolean reported = false;

    private Boolean isQueueInit = false;

    protected static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

    private static final String DEFAULT_THREAD_POOL_PREFIX = "DefaultThreadPool";

    private static List<EnhanceThreadPoolExecutor> EXIST_THREAD_POOL_EXECUTOR_LIST = new ArrayList<>();

    @Deprecated
    public EnhanceThreadPoolExecutor(int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue) {

        this(null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, defaultHandler);
    }

    @Deprecated
    public EnhanceThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler) {
        this(null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, null, handler);
    }

    @Deprecated
    public EnhanceThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory) {
        this(null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    @Deprecated
    public EnhanceThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        this(null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public EnhanceThreadPoolExecutor(String poolName,
                                     int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue) {
        this(poolName, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, defaultHandler);
    }

    public EnhanceThreadPoolExecutor(
            String poolName,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory) {
        this(poolName, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    public EnhanceThreadPoolExecutor(
            String poolName,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler) {
        this(poolName, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, null, handler);
    }

    public EnhanceThreadPoolExecutor(
            String poolName,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        threadPoolTagId = threadPoolTag.getAndIncrement();
        this.poolName = poolName == null ? DEFAULT_THREAD_POOL_PREFIX + "-" + threadPoolTagId : poolName;
        setThreadFactory(threadFactory == null ? new MonitorThreadFactory(this.poolName) : threadFactory);
        this.rejectHandlerName = handler.getClass().getSimpleName();
        RejectedExecutionHandler rejectedExecutionHandler = delegate(handler);
        setRejectedExecutionHandler(rejectedExecutionHandler);
        ThreadPoolStaticMetrics.bindTo(this);
        EXIST_THREAD_POOL_EXECUTOR_LIST.add(this);
//        if (isTaskPriority()) {
//            prestartAllCoreThreads();
//        }
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (!reported) {
            ThreadPoolStaticMetrics.bindTo(this);
        }
        startTimeThreadLocal.set(System.currentTimeMillis());
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        long costTime = System.currentTimeMillis() - startTimeThreadLocal.get();
        startTimeThreadLocal.remove();
        maxCostTime = maxCostTime > costTime ? maxCostTime : costTime;
        if (getCompletedTaskCount() == 0) {
            minCostTime = costTime;
        }
        minCostTime = minCostTime < costTime ? minCostTime : costTime;
        totalCostTime.addAndGet(costTime);
        super.afterExecute(r, t);
    }

    @Override
    public void execute(Runnable command) {
        Runnable wrappedRunnable = CatWrapper.wrap(MdcWrapper.wrap(command), poolName);
        if (isTaskPriority()) {
            executeTaskPriority(wrappedRunnable);
        } else {
            super.execute(wrappedRunnable);
        }
    }

    private void executeTaskPriority(Runnable command) {
        initQueue();
        if (command == null) {
            throw new NullPointerException();
        }

        try {
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            /*
             * 由于重写了队列的offer方法,可能存在没有放进队列就直接被reject的任务,因此重新尝试放入队列
             */
            if (super.getQueue() instanceof PriorityBlockingQueue) {
                final PriorityBlockingQueue queue = (PriorityBlockingQueue)super.getQueue();
                if (!queue.retryOffer(command)) {
                    throw new RejectedExecutionException(rx);
                }
            } else {
                throw rx;
            }
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    private void initQueue() {
        if (!isQueueInit) {
            if (super.getQueue() instanceof PriorityBlockingQueue) {
                final PriorityBlockingQueue queue = (PriorityBlockingQueue)super.getQueue();
                queue.setExecutor(this);
            }
            isQueueInit = true;
        }
    }

    public long getMinCostTime() {
        return minCostTime;
    }

    public long getMaxCostTime() {
        return maxCostTime;
    }

    public String getCurrentPoolName() {
        return poolName;
    }

    public int getWaitTaskCount() {
        return this.getQueue().size();
    }

    public int getQueueCapacity() {
        return this.getQueue().size() + this.getQueue().remainingCapacity();
    }

    public int getQueueSize() {
        return this.getQueue().size();
    }

    public int getQueueRemainingCapacity() {
        return this.getQueue().remainingCapacity();
    }

    public int getRejectCount() {
        return this.rejectCount.get();
    }

    public long getAverageCostTime() {//平均耗时
        if (getCompletedTaskCount() == 0 || totalCostTime.get() == 0) {
            return 0;
        }
        return totalCostTime.get() / getCompletedTaskCount();
    }

    public void incRejectCount(int count) {
        rejectCount.addAndGet(count);
    }

    public String getRejectType() {
        return this.rejectHandlerName;
    }

    protected static class MonitorThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        MonitorThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = poolName + "-thread-" +
                    poolNumber.getAndIncrement();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    public static RejectedExecutionHandler delegate(RejectedExecutionHandler handler) {
        return new RejectedInvocationHandler(handler);
    }

    public Integer getThreadPoolTagId() {
        return threadPoolTagId;
    }

    public static List<EnhanceThreadPoolExecutor> getExistThreadPoolExecutorList() {
        return EXIST_THREAD_POOL_EXECUTOR_LIST;
    }

    public void reported() {
        this.reported = true;
    }

    private Boolean isTaskPriority() {
        BlockingQueue<Runnable> queue = getQueue();
        return queue != null && getQueue() instanceof PriorityBlockingQueue;
    }
}
