package com.jindi.infra.trace.decorator;

import com.jindi.infra.trace.utils.TraceMDCUtil;

import feign.Request;
import feign.Response;
import feign.opentracing.FeignSpanDecorator;
import io.opentracing.Span;

public class FeignLoggerTraceSpanDecorator implements FeignSpanDecorator {

	@Override
	public void onRequest(Request request, Request.Options options, Span span) {
		TraceMDCUtil.putTraceInfo(span.context());
	}

	@Override
	public void onResponse(Response response, Request.Options options, Span span) {
		TraceMDCUtil.clear();
	}

	@Override
	public void onError(Exception exception, Request request, Span span) {
		TraceMDCUtil.clear();
	}
}
