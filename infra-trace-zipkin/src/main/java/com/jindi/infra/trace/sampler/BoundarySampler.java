package com.jindi.infra.trace.sampler;

import java.util.Random;

/**
 * 采集率支持 0.01% - 100%，比较适合高QPS的场景
 */
public final class BoundarySampler extends Sampler {

    private final long boundary;

    static final long SALT = new Random().nextLong();

    BoundarySampler(long boundary) {
        this.boundary = boundary;
    }

    /**
     * @param probability 0 全部不采集, 1 全部采集；
     * 最小采集率为 0.0001/0.01%;
     */
    public static Sampler create(float probability) {
        if (probability == 0) {
            return Sampler.NEVER_SAMPLE;
        }
        if (probability == 1.0) {
            return Sampler.ALWAYS_SAMPLE;
        }
        if (probability < 0.0001f || probability > 1) {
            throw new IllegalArgumentException(String.format("probability should be between 0.0001 and 1: was %s", probability));
        }
        final long boundary = (long) (probability * 10000);
        return new BoundarySampler(boundary);
    }

    /**
     * 当 {@code abs(traceId) <= boundary} 时，返回true；
     */
    @Override
    public boolean isSampled(long traceId) {
        long t = Math.abs(traceId ^ SALT);
        return t % 10000 <= boundary;
    }

    @Override
    public String toString() {
        return String.format("BoundaryTraceIdSampler(%s)", boundary);
    }
}

