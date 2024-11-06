package com.jindi.infra.metrics.thread;


import com.jindi.infra.tools.thread.EnhanceThreadPoolExecutor;
import com.jindi.infra.tools.thread.ThreadPoolStaticMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolInitReporter {

    public ThreadPoolInitReporter(List<ThreadPoolExecutor> threadPoolExecutorList, MeterRegistry meterRegistry) {
        if (meterRegistry != null) {
            ThreadPoolStaticMetrics.setLocalMeterRegistry(meterRegistry);
        }
        register(EnhanceThreadPoolExecutor.getExistThreadPoolExecutorList());
        registerAllBean(threadPoolExecutorList);
    }

    private void register(List<EnhanceThreadPoolExecutor> executorList) {
        if (CollectionUtils.isEmpty(executorList)) {
            return;
        }
        for (EnhanceThreadPoolExecutor enhanceThreadPoolExecutor : executorList) {
            ThreadPoolStaticMetrics.bindTo(enhanceThreadPoolExecutor);
        }
    }

    private void registerAllBean(List<ThreadPoolExecutor> executorList) {
        if (CollectionUtils.isEmpty(executorList)) {
            return;
        }
        for (ThreadPoolExecutor enhanceThreadPoolExecutor : executorList) {
            ThreadPoolStaticMetrics.bindTo(enhanceThreadPoolExecutor);
        }
    }

}
