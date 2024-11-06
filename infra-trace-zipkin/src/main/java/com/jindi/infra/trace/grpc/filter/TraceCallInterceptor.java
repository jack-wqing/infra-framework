package com.jindi.infra.trace.grpc.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.utils.TraceUtil;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.trace.grpc.context.GrpcTraceContext;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;

import io.grpc.MethodDescriptor;

public class TraceCallInterceptor implements CallInterceptor {

	public final Cache<Long, Span> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS)
			.maximumSize(10000).build();

	private final TraceContext traceContext;

	public TraceCallInterceptor(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Resource
	private GrpcTraceContext grpcTraceContext;

	@Value("${grpc.filter.full.name:infra.TycExtend/ping,infra.TycExtend}")
	private HashSet<String> filterFullNames;

	@Override
	public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
		if (filterFullNames.contains(method.getFullMethodName())) {
			return;
		}
		Span span = grpcTraceContext.buildClientTraceContext(method, extHeaders);
		if (span == null || id == null) {
			return;
		}
		cache.put(id, span);

	}

	@Override
	public void after(Long id, MethodDescriptor method, Throwable cause) {
		if (filterFullNames.contains(method.getFullMethodName())) {
			return;
		}
		if (id == null) {
			return;
		}
		Span span = cache.getIfPresent(id);
		if (cause != null) {
			TraceUtil.tag(span, TagsConsts.ERROR, cause.getMessage());
		}
		traceContext.writeSpan(span);
		cache.invalidate(id);
	}
}
