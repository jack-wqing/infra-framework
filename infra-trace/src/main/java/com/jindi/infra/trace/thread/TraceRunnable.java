package com.jindi.infra.trace.thread;

import java.util.Objects;

import com.jindi.infra.trace.constant.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * 增强Runnable，支持trace在线程池传递
 *
 * @author changbo
 * @date 2021/8/9
 */
public class TraceRunnable implements Runnable {

	private Span span;
	private Tracer tracer;
	private Runnable task;

	private TraceRunnable(Runnable task) {
		this.task = task;
		tracer = TracePropagation.tracer;
		if (tracer != null) {
			span = tracer.activeSpan();
		}
	}

	public static Runnable wrap(Runnable task) {
		Objects.requireNonNull(task);
		return new TraceRunnable(task);
	}

	@Override
	public void run() {
		if (tracer == null) {
			task.run();
			return;
		}
		Scope scope = this.span == null ? null : this.tracer.scopeManager().activate(this.span);
		try {
			TraceMDCUtil.putTraceInfo(span.context());
			task.run();
		} finally {
			if (scope != null) {
				scope.close();
			}
			TraceMDCUtil.clear();
		}
	}
}
