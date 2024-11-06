package com.jindi.infra.trace.decorator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.opentracing.Span;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;

public class HttpLoggerTraceSpanDecorator implements ServletFilterSpanDecorator {

	@Override
	public void onRequest(HttpServletRequest httpServletRequest, Span span) {
		TraceMDCUtil.putTraceInfo(span.context());
	}

	@Override
	public void onResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Span span) {
		TraceMDCUtil.clear();
	}

	@Override
	public void onError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Throwable exception, Span span) {
		TraceMDCUtil.clear();
	}

	@Override
	public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long timeout,
			Span span) {
		TraceMDCUtil.clear();
	}
}
