package com.jindi.infra.trace.grpc.interceptor;

import static com.jindi.infra.trace.constant.TracePropagation.GRPC;
import static com.jindi.infra.trace.constant.TracePropagation.PROTOCOL;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jindi.infra.grpc.constant.ProtoGoogleJavaMapping;
import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.trace.grpc.adapter.GrpcHeadersInjectAdapter;

import io.grpc.MethodDescriptor;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraceGrpcCallInterceptor implements CallInterceptor {

	private final Cache<Long, Span> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS)
			.maximumSize(20000).build();

	private Tracer tracer;

	public TraceGrpcCallInterceptor(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
		String fullMethodName = method.getFullMethodName();
		if (fullMethodName.endsWith("/ping")) {
			return;
		}
		// 将google包名转换成java包名
		fullMethodName = ProtoGoogleJavaMapping.convert(fullMethodName);
		Span span = tracer.buildSpan("客户端: " + fullMethodName).withTag(PROTOCOL, GRPC)
				.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
		SpanContext context = span.context();
		JaegerSpanContext spanContext = (JaegerSpanContext) context;
		tracer.inject(spanContext, Format.Builtin.HTTP_HEADERS, new GrpcHeadersInjectAdapter(extHeaders));
		if (spanContext.isSampled()) {
			cache.put(id, span);
		}
	}

	@Override
	public void after(Long id, MethodDescriptor method, Throwable cause) {
		Span span = cache.getIfPresent(id);
		if (span == null) {
			return;
		}
		span.finish();
		cache.invalidate(id);
	}

}
