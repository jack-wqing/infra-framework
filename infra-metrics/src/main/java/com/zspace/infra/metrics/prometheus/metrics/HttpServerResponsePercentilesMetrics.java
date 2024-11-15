package com.zspace.infra.metrics.prometheus.metrics;

import static com.zspace.infra.metrics.constant.MetricsConstant.PERCENTILE_SWITCH;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.core.env.Environment;

import com.zspace.infra.metrics.prometheus.util.MetricsConfigUtil;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

/**
 * http分位线指标
 */
public class HttpServerResponsePercentilesMetrics implements MeterRegistryCustomizer {

    @Autowired
    private Environment environment;

    /**
     * 在原有Web Metrics基础上，增加percentilesHistogram监控
     */
    @Override
    public void customize(MeterRegistry registry) {
        registry.config().meterFilter(
                new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                        if (id.getType() == Meter.Type.TIMER && id.getName().matches("^(http.server){1}.*")) {
                            double[] slo = MetricsConfigUtil.getBucketsToNanos(environment);
                            double[] percentiles = MetricsConfigUtil.getPercentiles(environment);
                            DistributionStatisticConfig.Builder builder = DistributionStatisticConfig.builder();
                            builder.serviceLevelObjectives(slo).expiry(Duration.ofMinutes(1));
                            if (isOpenPercentile()) {
                                builder.percentiles(percentiles);
                            }
                            return builder.build().merge(config);
                        } else {
                            return config;
                        }
                    }
                });
    }

    /**
     * 开启固定分位线百分比
     */
    public boolean isOpenPercentile() {
        String percentileSwitch = environment.getProperty(PERCENTILE_SWITCH);
        if ("false".equals(percentileSwitch)) {
            return false;
        }
        return true;
    }
}
