package com.jindi.infra.trace.sampler;

/**
 * sampler(采样器) 负责决定一条 trace(链路) 是否应该被采样；
 * 若被采集，trace 信息会沿着链路往下游服务传递，并且服务会利用 reporter 上报 trace 链路信息；
 */
public abstract class Sampler {

    /**
     * 采集所有trace链路
     */
    public static final Sampler ALWAYS_SAMPLE = new Sampler() {
        @Override
        public boolean isSampled(long traceId) {
            return true;
        }
        @Override
        public String toString() {
            return "AlwaysSample";
        }
    };

    /**
     * 所有trace链路皆不采集
     */
    public static final Sampler NEVER_SAMPLE = new Sampler() {
        @Override
        public boolean isSampled(long traceId) {
            return false;
        }
        @Override
        public String toString() {
            return "NeverSample";
        }
    };

    /**
     * 如果该条链路需要被采集，则返回true；
     */
    public abstract boolean isSampled(long traceId);

    /**
     * 根据给出的采集率，创建一个 sampler 以供使用；
     * {@link BoundarySampler} 采集率支持 0.01% - 100%，比较适合高QPS的场景
     */
    public static Sampler create(float rate) {
        return BoundarySampler.create(rate);
    }

}
