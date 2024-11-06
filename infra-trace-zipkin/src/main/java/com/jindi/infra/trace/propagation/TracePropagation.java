package com.jindi.infra.trace.propagation;

public class TracePropagation {

    public static final String TRACE_ID_KEY_B3_OLD = "X-B3-TraceId";
    public static final String SPAN_ID_KEY_B3_OLD = "X-B3-SpanId";
    public static final String PARENT_SPAN_ID_KEY_B3_OLD = "X-B3-ParentSpanId";
    public static final String SAMPLED_KEY_B3_OLD = "X-B3-Sampled";

    public static final String TRACE_ID_KEY_B3 = "traceId";
    public static final String SPAN_ID_KEY_B3 = "spanId";
    public static final String PARENT_SPAN_ID_KEY_B3 = "parentId";
    public static final String SAMPLED_KEY_B3 = "sampled";
    public static final String EXTRA_KEY_B3 = "extra";

    public static final String TRACE_ID_KEY = "x-b3-traceid-jindi";
    public static final String SPAN_ID_KEY = "x-b3-spanid-jindi";
    public static final String PARENT_ID_KEY = "x-b3-parentspanid-jindi";
    public static final String SAMPLED_KEY = "x-b3-sampled-jindi";
    public static final String EXTRA_KEY = "x-b3-extra-jindi";

    public static final String APPLICATION_NAME = "applicationName";

    private String traceId;

    private String spanId;

    private String parentSpanId;

    private String sampled;

    private String extra;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSampled() {
        return sampled;
    }

    public void setSampled(String sampled) {
        this.sampled = sampled;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "TracePropagation{" +
                "traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", parentSpanId='" + parentSpanId + '\'' +
                ", sampled='" + sampled + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}

