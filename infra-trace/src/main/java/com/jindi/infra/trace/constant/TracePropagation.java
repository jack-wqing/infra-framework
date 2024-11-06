package com.jindi.infra.trace.constant;

import io.opentracing.Tracer;

public class TracePropagation {

	public static Tracer tracer;

	public static final String TRACE_ID_KEY = "traceId";
	public static final String SPAN_ID_KEY = "spanId";
	public static final String PARENT_ID_KEY = "parentId";
	public static final String SAMPLED_KEY = "sampled";

	public static final String TRACE_ID_KEY_JINDI = "x-b3-traceid-jindi";
	public static final String SPAN_ID_KEY_JINDI = "x-b3-spanid-jindi";
	public static final String PARENT_ID_KEY_JINDI = "x-b3-parentspanid-jindi";
	/** 1表示采样 */
	public static final String SAMPLED_KEY_JINDI = "x-b3-sampled-jindi";

	public static final String APPLICATION_NAME = "applicationName";

	public static final String PROTOCOL = "protocol";
	public static final String DUBBO = "dubbo";
	public static final String GRPC = "grpc";
	public static final String SERVICE = "service";

	private String traceId;

	private String spanId;

	private String parentSpanId;

	private String sampled;

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

}
