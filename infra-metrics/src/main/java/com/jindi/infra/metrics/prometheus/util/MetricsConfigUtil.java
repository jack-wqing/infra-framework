package com.jindi.infra.metrics.prometheus.util;

import static com.jindi.infra.metrics.constant.MetricsConstant.BUCKET_LIST;
import static com.jindi.infra.metrics.constant.MetricsConstant.PERCENTILE_VALUE;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

public class MetricsConfigUtil {

    /**
     * 解析bucket列表
     */
    public static double[] getBucketsToNanos(Environment environment) {
        String buckets = environment.getProperty(BUCKET_LIST);
        if (StringUtils.isBlank(buckets)) {
            return null;
        }
        String[] bucketArr = buckets.split(",");
        double[] result = new double[bucketArr.length];
        for (int i = 0; i < bucketArr.length; i++) {
            result[i] = Duration.ofMillis(Long.parseLong(bucketArr[i])).toNanos();
        }
        return result;
    }

    public static double[] getBucketsToSeconds(Environment environment) {
        String buckets = environment.getProperty(BUCKET_LIST);
        if (StringUtils.isBlank(buckets)) {
            return null;
        }
        String[] bucketArr = buckets.split(",");
        double[] result = new double[bucketArr.length];
        for (int i = 0; i < bucketArr.length; i++) {
            result[i] = Integer.parseInt(bucketArr[i]) / 1000.0;
        }
        return result;
    }

    public static Duration[] getBucketsToDuration(Environment environment) {
        String buckets = environment.getProperty(BUCKET_LIST);
        if (StringUtils.isBlank(buckets)) {
            return null;
        }
        String[] bucketArr = buckets.split(",");
        Duration[] result = new Duration[bucketArr.length];
        for (int i = 0; i < bucketArr.length; i++) {
            result[i] = Duration.ofMillis(Long.parseLong(bucketArr[i]));
        }
        return result;
    }

    public static double[] getPercentiles(Environment environment) {
        String percentiles = environment.getProperty(PERCENTILE_VALUE);
        if (StringUtils.isBlank(percentiles)) {
            return null;
        }
        String[] percentileArr = percentiles.split(",");
        double[] result = new double[percentileArr.length];
        for (int i = 0; i < percentileArr.length; i++) {
            result[i] = Double.parseDouble(percentileArr[i]);
        }
        return result;
    }

}
