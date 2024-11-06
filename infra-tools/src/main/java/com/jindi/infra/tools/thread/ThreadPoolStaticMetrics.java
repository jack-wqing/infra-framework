package com.jindi.infra.tools.thread;


import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.ToDoubleFunction;

@Slf4j
public class ThreadPoolStaticMetrics  {

    @Resource
    private MeterRegistry meterRegistry;

    private static MeterRegistry localMeterRegistry;

    public static final String TYC_EXECUTOR_METRIC_NAME_PREFIX = "tyc.thread.pool";

    public static final String BASE_THREAD_POOL_PREFIX = "base-thread-pool-";

    private static final Integer MAX_LENGTH = 1000;

    private static final AtomicLongArray BINDED_THREAD_POOL = new AtomicLongArray(MAX_LENGTH);

    private static final AtomicInteger BASE_THREAD_POOL_TAG_ID = new AtomicInteger(1);

    public static void bindTo(ThreadPoolExecutor threadPoolExecutor) {
        if (localMeterRegistry == null) {
            return;
        }
        try {
            doBind(threadPoolExecutor);
        } catch (Exception e) {
            log.debug("线程池上报异常", e);
        }
    }

    public static void bindTo(String poolName, ThreadPoolExecutor threadPoolExecutor) {
        if (localMeterRegistry == null) {
            return;
        }
        try {
            reportBaseMetric(poolName, threadPoolExecutor, localMeterRegistry);
        } catch (Exception e) {
            log.debug("线程池上报异常", e);
        }
    }

    private static void doBind(ThreadPoolExecutor threadPoolExecutor) {
        if (threadPoolExecutor instanceof EnhanceThreadPoolExecutor) {
            EnhanceThreadPoolExecutor enhanceThreadPoolExecutor = (EnhanceThreadPoolExecutor) threadPoolExecutor;
            if (enhanceThreadPoolExecutor.getThreadPoolTagId() > MAX_LENGTH) {
                log.debug("线程池上报数量超过限制, limit:{}", MAX_LENGTH);
                return;
            }
            if (BINDED_THREAD_POOL.compareAndSet(enhanceThreadPoolExecutor.getThreadPoolTagId(), 0L, 1L)) {
                report((EnhanceThreadPoolExecutor) threadPoolExecutor, localMeterRegistry);
                ((EnhanceThreadPoolExecutor) threadPoolExecutor).reported();
            }
        } else {
            reportBaseMetric(threadPoolExecutor, localMeterRegistry);
        }
    }

    private static void reportBaseMetric(ThreadPoolExecutor threadPoolExecutor, MeterRegistry registry) {
        String threadPoolName = BASE_THREAD_POOL_PREFIX + BASE_THREAD_POOL_TAG_ID.getAndIncrement();
        reportBaseMetric(threadPoolName, threadPoolExecutor, registry);
    }

    private static void reportBaseMetric(String threadPoolName, ThreadPoolExecutor threadPoolExecutor, MeterRegistry registry) {
        gauge("core.size", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getCorePoolSize, registry);
        gauge("maximum.size", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getMaximumPoolSize, registry);
        gauge("current.size", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getPoolSize, registry);
        gauge("largest.size", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getLargestPoolSize, registry);
        gauge("active.count", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getActiveCount, registry);
        gauge("task.count", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getTaskCount, registry);
        gauge("completed.task.count", threadPoolName, threadPoolExecutor, ThreadPoolExecutor::getCompletedTaskCount, registry);
        gauge("wait.task.count", threadPoolName, threadPoolExecutor, (executor) -> executor.getQueue().size(), registry);
        gauge("queue.size", threadPoolName, threadPoolExecutor, (executor) -> executor.getQueue().size(), registry);
        gauge("queue.capacity", threadPoolName, threadPoolExecutor, (executor) -> executor.getQueue().size() + executor.getQueue().remainingCapacity(), registry);
        gauge("queue.remaining.capacity", threadPoolName, threadPoolExecutor, (executor) -> executor.getQueue().remainingCapacity(), registry);
    }

    private static void report(EnhanceThreadPoolExecutor monitorExecutor, MeterRegistry registry) {
        gauge("core.size", monitorExecutor, EnhanceThreadPoolExecutor::getCorePoolSize, registry);
        gauge("maximum.size", monitorExecutor, EnhanceThreadPoolExecutor::getMaximumPoolSize, registry);
        gauge("current.size", monitorExecutor, EnhanceThreadPoolExecutor::getPoolSize, registry);
        gauge("largest.size", monitorExecutor, EnhanceThreadPoolExecutor::getLargestPoolSize, registry);
        gauge("active.count", monitorExecutor, EnhanceThreadPoolExecutor::getActiveCount, registry);
        gauge("task.count", monitorExecutor, EnhanceThreadPoolExecutor::getTaskCount, registry);
        gauge("completed.task.count", monitorExecutor, EnhanceThreadPoolExecutor::getCompletedTaskCount, registry);
        gauge("wait.task.count", monitorExecutor, EnhanceThreadPoolExecutor::getWaitTaskCount, registry);
        gauge("queue.size", monitorExecutor, EnhanceThreadPoolExecutor::getQueueSize, registry);
        gauge("queue.capacity", monitorExecutor, EnhanceThreadPoolExecutor::getQueueCapacity, registry);
        gauge("queue.remaining.capacity", monitorExecutor, EnhanceThreadPoolExecutor::getQueueRemainingCapacity, registry);
        gauge("reject.count", monitorExecutor, EnhanceThreadPoolExecutor::getRejectCount, registry, "rejectType", monitorExecutor.getRejectType());
        gauge("min.cost.time", monitorExecutor, EnhanceThreadPoolExecutor::getMinCostTime, registry);
        gauge("max.cost.time", monitorExecutor, EnhanceThreadPoolExecutor::getMaxCostTime, registry);
        gauge("avg.cost.time", monitorExecutor, EnhanceThreadPoolExecutor::getAverageCostTime, registry);
    }

    private static void gauge(String metricName, String threadPoolName, ThreadPoolExecutor executor, ToDoubleFunction<ThreadPoolExecutor> function, MeterRegistry registry, String... tagKeyValues) {
        Tags tags = Tags.of( "name", threadPoolName);
        Gauge.builder(toMetricName(metricName), executor, function)
                .tags(tags.and(tagKeyValues))
                .description(metricName).baseUnit("threads").register(registry);
    }

    private static void gauge(String metricName, EnhanceThreadPoolExecutor executor, ToDoubleFunction<EnhanceThreadPoolExecutor> function, MeterRegistry registry, String... tagKeyValues) {
        Tags tags = Tags.of( "name", executor.getCurrentPoolName());
        Gauge.builder(toMetricName(metricName), executor, function)
                .tags(tags.and(tagKeyValues))
                .description(metricName).baseUnit("threads").register(registry);
    }

    private static String toMetricName(String name) {
        return String.join(".", TYC_EXECUTOR_METRIC_NAME_PREFIX, name);
    }

    @PostConstruct
    public void init() {
        localMeterRegistry = meterRegistry;
    }

    public static void setLocalMeterRegistry(MeterRegistry localMeterRegistry) {
        ThreadPoolStaticMetrics.localMeterRegistry = localMeterRegistry;
    }
}