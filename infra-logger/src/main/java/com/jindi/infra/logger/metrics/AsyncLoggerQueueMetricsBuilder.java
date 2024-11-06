package com.jindi.infra.logger.metrics;

import static com.jindi.infra.logger.loader.PodFileConsoleRollAppenderLoader.podFileConsoleAsyncAppender;
import static com.jindi.infra.logger.loader.PodFileErrorRollAppenderLoader.podFileErrorAsyncAppender;
import static com.jindi.infra.logger.loader.PodFileMainRollAppenderLoader.podFileMainAsyncAppender;

import ch.qos.logback.classic.AsyncAppender;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

public class AsyncLoggerQueueMetricsBuilder implements MeterBinder {

    @Override
    public void bindTo(MeterRegistry registry) {
        reportLoggerPodFileMainQueueMetrics(registry);
        reportLoggerPodFileErrorQueueMetrics(registry);
        reportLoggerPodFileConsoleQueueMetrics(registry);
    }

    protected void reportLoggerPodFileMainQueueMetrics(MeterRegistry meterRegistry) {
        AsyncAppender asyncAppender = podFileMainAsyncAppender.get();
        if (asyncAppender != null) {
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getRemainingCapacity())
                    .tags(Tags.of("output", "main").and("type", "remainingCapacity")).register(meterRegistry);
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getQueueSize())
                    .tags(Tags.of("output", "main").and("type", "queueSize")).register(meterRegistry);
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getNumberOfElementsInQueue())
                    .tags(Tags.of("output", "main").and("type", "numberOfElements")).register(meterRegistry);
        }
    }

    protected void reportLoggerPodFileErrorQueueMetrics(MeterRegistry meterRegistry) {
        AsyncAppender asyncAppender = podFileErrorAsyncAppender.get();
        if (asyncAppender != null) {
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getRemainingCapacity())
                    .tags(Tags.of("output", "error").and("type", "remainingCapacity")).register(meterRegistry);
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getQueueSize())
                    .tags(Tags.of("output", "error").and("type", "queueSize")).register(meterRegistry);
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getNumberOfElementsInQueue())
                    .tags(Tags.of("output", "error").and("type", "numberOfElements")).register(meterRegistry);
        }
    }

    protected void reportLoggerPodFileConsoleQueueMetrics(MeterRegistry meterRegistry) {
        AsyncAppender asyncAppender = podFileConsoleAsyncAppender.get();
        if (asyncAppender != null) {
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getRemainingCapacity())
                    .tags(Tags.of("output", "console").and("type", "remainingCapacity")).register(meterRegistry);
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getQueueSize())
                    .tags(Tags.of("output", "console").and("type", "queueSize")).register(meterRegistry);
            Gauge.builder("logger.pod.file.async.queue", asyncAppender, appender -> appender.getNumberOfElementsInQueue())
                    .tags(Tags.of("output", "console").and("type", "numberOfElements")).register(meterRegistry);
        }
    }
}
