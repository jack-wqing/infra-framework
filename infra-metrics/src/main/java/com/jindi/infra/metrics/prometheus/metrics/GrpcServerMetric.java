package com.jindi.infra.metrics.prometheus.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.jindi.infra.metrics.prometheus.util.MetricsConfigUtil;

import io.prometheus.client.CollectorRegistry;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;

public class GrpcServerMetric {

    @Autowired
    private Environment environment;

    public MonitoringServerInterceptor createGrpcServerInterceptor(CollectorRegistry collectorRegistry) {
        return MonitoringServerInterceptor.create(
                Configuration.allMetrics().withLatencyBuckets(MetricsConfigUtil.getBucketsToSeconds(environment)).withCollectorRegistry(collectorRegistry));
    }
}
