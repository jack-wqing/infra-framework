package com.jindi.infra.trace.dubbo.filter;

import static com.jindi.infra.trace.constant.TracePropagation.*;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.jindi.infra.trace.dubbo.adapter.DubboHeadersInjectAdapter;
import com.jindi.infra.trace.utils.SpringBeanUtils;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Activate(group = Constants.CONSUMER)
public class DubboClientFilter implements Filter {

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Tracer tracer = SpringBeanUtils.getBean(Tracer.class);
		Span span = tracer.buildSpan("客户端: " + invocation.getMethodName()).withTag(PROTOCOL, DUBBO)
				.withTag(SERVICE, invoker.getUrl().getPath()).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
				.start();
		SpanContext context = span.context();
		JaegerSpanContext spanContext = (JaegerSpanContext) context;
		tracer.inject(spanContext, Format.Builtin.HTTP_HEADERS,
				new DubboHeadersInjectAdapter(invocation.getAttachments()));
		Result result;
		try {
			result = invoker.invoke(invocation);
		} catch (Throwable e) {
			throw e;
		} finally {
			span.finish();
		}
		return result;
	}
}
