package com.jindi.infra.trace.thread;

import java.util.concurrent.Callable;

import com.jindi.infra.trace.constant.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * 增强Callable，支持trace在线程池传递
 *
 * @author changbo
 * @date 2021/8/9
 */
public class TraceCallable<T> implements Callable<T> {

	private Span span;
	private Tracer tracer;
	private Callable<T> task;

	private TraceCallable(Callable<T> task) {
		this.task = task;
		tracer = TracePropagation.tracer;
		if (tracer != null) {
			span = tracer.activeSpan();
		}
	}

	public static <T> Callable wrap(Callable<T> callable) {
		return new TraceCallable(callable);
	}

	@Override
	public T call() throws Exception {
		if (tracer == null) {
			return task.call();
		}

		Scope scope = span == null ? null : tracer.scopeManager().activate(span);
		try {
			TraceMDCUtil.putTraceInfo(span.context());
			return task.call();
		} finally {
			if (scope != null) {
				scope.close();
			}
			TraceMDCUtil.clear();
		}
	}
}
