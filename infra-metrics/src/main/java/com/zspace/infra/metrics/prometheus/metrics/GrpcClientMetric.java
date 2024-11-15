package com.zspace.infra.metrics.prometheus.metrics;

import com.zspace.infra.metrics.prometheus.util.MetricsConfigUtil;
import io.prometheus.client.CollectorRegistry;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class GrpcClientMetric {

    @Autowired
    private Environment environment;

    public MonitoringClientInterceptor createGrpcClientInterceptor(CollectorRegistry collectorRegistry) {
        return MonitoringClientInterceptor.create(
                Configuration.allMetrics().withLatencyBuckets(MetricsConfigUtil.getBucketsToSeconds(environment)).withCollectorRegistry(collectorRegistry));
    }
}
