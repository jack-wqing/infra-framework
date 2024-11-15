package com.zspace.infra.metrics.prometheus.metrics;


import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;


public class ContainerProcessCpuLoadMetrics  implements MeterBinder {
    private static final String METRIC_NAME = "container_cpu_process_usage";
    volatile long processCpuTime = 0L;
    volatile long processUpTime = 0L;

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        Gauge.builder(METRIC_NAME, this, cpu -> getContainerCpu()).register(registry);
    }

    public double getContainerCpu() {
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long newProcessCpuTime = bean.getProcessCpuTime();
        long newProcessUpTime = ManagementFactory.getRuntimeMXBean().getUptime();
        int cpuCores = bean.getAvailableProcessors();
        long processCpuTimeDiffInMs = TimeUnit.NANOSECONDS.toMillis(newProcessCpuTime - this.processCpuTime);
        long processUpTimeDiffInMs = newProcessUpTime - this.processUpTime;
        double processCpuUsage = (double)processCpuTimeDiffInMs / (double)processUpTimeDiffInMs / (double)cpuCores;
        this.processUpTime = newProcessUpTime;
        this.processCpuTime = newProcessCpuTime;
        return processCpuUsage;
    }
}