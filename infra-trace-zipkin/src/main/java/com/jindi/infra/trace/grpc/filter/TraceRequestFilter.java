package com.jindi.infra.trace.grpc.filter;

import com.jindi.infra.common.constant.GrpcContextConstant;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.grpc.util.GrpcContextUtils;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.utils.TraceUtil;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;

public class TraceRequestFilter implements RequestFilter {

	private final TraceContext traceContext;

	public TraceRequestFilter(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Value("${grpc.filter.full.name:infra.TycExtend/ping,infra.TycExtend}")
	private HashSet<String> filterFullNames;

	@Override
	public void before(Long id, MethodDescriptor method, Metadata headers) {

	}

	@Override
	public void after(Long id, MethodDescriptor method, Throwable cause) {
		if (id == null || needIgnore(method.getFullMethodName())) {
			return;
		}
		String spanData = ContextUtils.getContextValue(GrpcContextConstant.OPENTRACING_SPAN);
		if (StringUtils.isBlank(spanData)) {
			return;
		}
		Span span = InnerJSONUtils.parseObject(spanData, Span.class);
		if (cause != null) {
			TraceUtil.tag(span, TagsConsts.ERROR, cause.getMessage());
		}
		fillCatMessageId(span);
		traceContext.writeSpan(span);
	}

	private void fillCatMessageId(Span span) {
		if (span == null || GrpcContextUtils.get(TagsConsts.CAT_MESSAGE) == null) {
			return;
		}
		span.tag(TagsConsts.CAT_MESSAGE, GrpcContextUtils.get(TagsConsts.CAT_MESSAGE));
	}

	private Boolean needIgnore(String methodName) {
		if (StringUtils.isBlank(methodName) || filterFullNames.contains(methodName)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
