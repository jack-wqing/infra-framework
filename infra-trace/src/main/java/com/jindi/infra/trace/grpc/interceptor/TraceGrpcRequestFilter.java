package com.jindi.infra.trace.grpc.interceptor;

import static com.jindi.infra.trace.constant.TracePropagation.GRPC;
import static com.jindi.infra.trace.constant.TracePropagation.PROTOCOL;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jindi.infra.grpc.constant.ProtoGoogleJavaMapping;
import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.trace.grpc.adapter.GrpcHeadersExtractAdapter;
import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraceGrpcRequestFilter implements RequestFilter {

	private final Cache<Long, Span> spanCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS)
			.maximumSize(2000).build();
	private final Cache<Long, Scope> scopeCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS)
			.maximumSize(2000).build();

	private Tracer tracer;

	public TraceGrpcRequestFilter(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void before(Long id, MethodDescriptor method, Metadata headers) {
		String fullMethodName = method.getFullMethodName();
		if (fullMethodName.endsWith("/ping")) {
			return;
		}
		// 将google包名转换成java包名
		fullMethodName = ProtoGoogleJavaMapping.convert(fullMethodName);
		SpanContext extractedContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
				new GrpcHeadersExtractAdapter(headers));
		Span span = tracer.buildSpan("服务端: " + fullMethodName).asChildOf(extractedContext).withTag(PROTOCOL, GRPC)
				.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
		JaegerSpanContext spanContext = (JaegerSpanContext) span.context();
		TraceMDCUtil.putTraceInfo(spanContext);
		if (spanContext.isSampled()) {
			spanCache.put(id, span);
		}
		Scope scope = tracer.activateSpan(span);
		scopeCache.put(id, scope);
	}

	@Override
	public void after(Long id, MethodDescriptor method, Throwable cause) {
		Scope scope = scopeCache.getIfPresent(id);
		if (scope != null) {
			scope.close();
			scopeCache.invalidate(id);
		}
		Span span = spanCache.getIfPresent(id);
		if (span != null) {
			span.finish();
			spanCache.invalidate(id);
		}
	}
}
