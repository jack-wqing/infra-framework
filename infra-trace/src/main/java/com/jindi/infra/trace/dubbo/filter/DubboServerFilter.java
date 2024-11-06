package com.jindi.infra.trace.dubbo.filter;

import static com.jindi.infra.trace.constant.TracePropagation.*;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.jindi.infra.trace.dubbo.adapter.DubboHeadersExtractAdapter;
import com.jindi.infra.trace.utils.SpringBeanUtils;
import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Activate(group = Constants.PROVIDER, order = -10000)
public class DubboServerFilter implements Filter {

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Tracer tracer = SpringBeanUtils.getBean(Tracer.class);
		SpanContext extractedContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
				new DubboHeadersExtractAdapter(invocation.getAttachments()));
		Span span = tracer.buildSpan("服务端: " + invocation.getMethodName()).asChildOf(extractedContext)
				.withTag(PROTOCOL, DUBBO).withTag(SERVICE, invoker.getUrl().getPath())
				.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
		JaegerSpanContext spanContext = (JaegerSpanContext) span.context();
		TraceMDCUtil.putTraceInfo(spanContext);
		Scope scope = tracer.activateSpan(span);
		Result result;
		try {
			result = invoker.invoke(invocation);
		} catch (Throwable e) {
			throw e;
		} finally {
			scope.close();
			if (spanContext.isSampled()) {
				span.finish();
			}
			TraceMDCUtil.clear();
		}
		return result;
	}
}
