package com.jindi.infra.metrics.prometheus.metrics;

import static com.jindi.infra.metrics.constant.MetricsConstant.DUBBO_SERVER_RESPONSE_METRICS;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.Environment;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.metrics.prometheus.util.MetricsConfigUtil;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Activate(group = Constants.PROVIDER, order = -20000)
public class DubboServerMetricsFilter implements Filter {

    private Environment environment;
    private MeterRegistry meterRegistry;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        meterRegistry = ACUtils.getBean(MeterRegistry.class);
        environment = ACUtils.getBean(Environment.class);
        List<Tag> tags = createTags(invoker, invocation);
        Timer.Sample timerSample = Timer.start(meterRegistry);
        Result result;
        try {
            result = invoker.invoke(invocation);
            timerSample.stop(buildTimer(tags));
        } catch (Throwable e) {
            timerSample.stop(buildTimer(tags, e));
            throw e;
        }
        return result;
    }

    private void addExceptionTag(List<Tag> tags, Throwable e) {
        if (e == null) {
            tags.add(Tag.of("exception", "None"));
        } else {
            tags.add(Tag.of("exception", e.getClass().getSimpleName()));
        }
    }

    private List<Tag> createTags(Invoker<?> invoker, Invocation invocation) {
        List<Tag> tags = new ArrayList<>();
        String className = invoker.getUrl().getPath();
        String methodName = invocation.getMethodName();
        Tag classTag = Tag.of("class", className);
        Tag methodTag = Tag.of("method", methodName);
        tags.add(classTag);
        tags.add(methodTag);
        return tags;
    }

    public Timer buildTimer(List<Tag> tags) {
        addExceptionTag(tags, null);
        return Timer.builder(DUBBO_SERVER_RESPONSE_METRICS)
                .distributionStatisticExpiry(Duration.ofMinutes(1))
                .tags(tags)
                .serviceLevelObjectives(MetricsConfigUtil.getBucketsToDuration(environment))
                .publishPercentiles(MetricsConfigUtil.getPercentiles(environment))
                .register(meterRegistry);
    }

    public Timer buildTimer(List<Tag> tags, Throwable e) {
        addExceptionTag(tags, e);
        return Timer.builder(DUBBO_SERVER_RESPONSE_METRICS)
                .distributionStatisticExpiry(Duration.ofMinutes(1))
                .tags(tags)
                .serviceLevelObjectives(MetricsConfigUtil.getBucketsToDuration(environment))
                .publishPercentiles(MetricsConfigUtil.getPercentiles(environment))
                .register(meterRegistry);
    }

}

