package com.jindi.infra.trace.model;

import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.reporter.FileSpanReporter;
import com.jindi.infra.trace.sampler.Sampler;
import com.jindi.infra.trace.utils.HexCodec;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class TraceContext {

    @Resource
    private FileSpanReporter reporter;
    @Resource
    private Sampler sampler;
    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${server.port}")
    private Integer port;
    private TickClock CLOCK = new TickClock();


    public void writeSpan(Span span) {
        if (span == null) {
            return;
        }
        span.setDuration(CLOCK.currentTimeMicroseconds() - span.getTimestamp());
        reporter.report(span);
    }

    public Span buildSpan(TracePropagation tracePropagation, String name, Span.KindEnum kind, String remoteServiceName) {
        if (!Objects.equals("1", tracePropagation.getSampled())) {
            return null;
        }
        Span span = new Span();
        if (StringUtils.isNotBlank(tracePropagation.getTraceId())) {
            span.setTraceId(tracePropagation.getTraceId());
        }
        if (StringUtils.isNotBlank(tracePropagation.getParentSpanId())) {
            span.setParentId(tracePropagation.getParentSpanId());
        }
        if(StringUtils.isNotBlank(tracePropagation.getSpanId())) {
            span.setId(tracePropagation.getSpanId());
        }
        span.setName(name);
        span.setKind(kind);
        span.setTimestamp(CLOCK.currentTimeMicroseconds());
        span.setTimestamp_millis(System.currentTimeMillis());
        span.setLocalEndpoint(createLocalPoint());
        span.setTags(new Tags());
        span.setAnnotations(new ArrayList<>());
        if (StringUtils.isNotBlank(remoteServiceName)) {
            span.setRemoteEndpoint(createRemotePoint(remoteServiceName));
        }
        return span;
    }

    public TracePropagation createTracePropagation() {
        TracePropagation tracePropagation = new TracePropagation();
        String traceId = createTraceId();
        tracePropagation.setTraceId(traceId);
        tracePropagation.setParentSpanId(null);
        tracePropagation.setSpanId(traceId);
        tracePropagation.setSampled(isSampled(traceId));
        tracePropagation.setExtra(null);
        TraceMDCUtil.putTraceInfo(tracePropagation);
        return tracePropagation;
    }

    public TracePropagation createTracePropagation(String traceId, String parentId, String spanId, String sampled, String extra) {
        TracePropagation tracePropagation = new TracePropagation();
        tracePropagation.setTraceId(traceId);
        tracePropagation.setParentSpanId(parentId);
        tracePropagation.setSpanId(spanId);
        tracePropagation.setSampled(StringUtils.isBlank(sampled) ? isSampled(traceId) : sampled);
        tracePropagation.setExtra(extra);
        TraceMDCUtil.putTraceInfo(tracePropagation);
        return tracePropagation;
    }

    public TracePropagation createCSTracePropagation(TracePropagation currentTracePropagation) {
        TracePropagation csTracePropagation = new TracePropagation();
        csTracePropagation.setTraceId(currentTracePropagation.getTraceId());
        csTracePropagation.setParentSpanId(currentTracePropagation.getSpanId());
        csTracePropagation.setSpanId(createTraceId());
        csTracePropagation.setSampled(currentTracePropagation.getSampled());
        csTracePropagation.setExtra(currentTracePropagation.getExtra());
        return csTracePropagation;
    }

    public String createTraceId() {
        return HexCodec.toLowerHex(RandomUtils.nextLong());
    }

    public String isSampled(String traceId) {
        boolean sampled = sampler.isSampled(HexCodec.lowerHexToUnsignedLong(traceId));
        return sampled ? "1" : "0";
    }

    public Endpoint createLocalPoint() {
        Endpoint localPoint = new Endpoint();
        if (StringUtils.isNotBlank(serviceName)) {
            localPoint.setServiceName(serviceName);
        }
        if (port != null) {
            localPoint.setPort(port);
        }
        localPoint.setIpv4(InnerIpUtils.getCachedIP());
        return localPoint;
    }

    public Endpoint createRemotePoint(String remoteServerName) {
        Endpoint remotePoint = new Endpoint();
        if (StringUtils.isNotBlank(remoteServerName)) {
            remotePoint.setServiceName(remoteServerName);
        }
        return remotePoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    final class TickClock {
        private final long baseEpochMicros;
        private final long baseTickNanos;

        TickClock() {
            this.baseEpochMicros = System.currentTimeMillis() * 1000L;
            this.baseTickNanos = System.nanoTime();
        }

        public long currentTimeMicroseconds() {
            return (System.nanoTime() - this.baseTickNanos) / 1000L + this.baseEpochMicros;
        }

        @Override
        public String toString() {
            return "TickClock{baseEpochMicros=" + this.baseEpochMicros + ", baseTickNanos=" + this.baseTickNanos + "}";
        }
    }
}

