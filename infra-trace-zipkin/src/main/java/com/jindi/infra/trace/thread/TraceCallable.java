package com.jindi.infra.trace.thread;

import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 增强Callable，支持MDC值在线程池传递
 *
 * @author changbo
 * @date 2021/8/9
 */
public class TraceCallable<T> implements Callable<T> {

	private Map<String, String> mdcMap;
	private Callable<T> task;

	private TraceCallable(Callable<T> task, Map<String, String> mdcMap) {
		this.task = task;
		this.mdcMap = mdcMap;
	}

	public static <T> Callable wrap(Callable<T> callable) {
		Objects.requireNonNull(callable);
		return new TraceCallable(callable, MDC.getCopyOfContextMap());
	}

	@Override
	public T call() throws Exception {
		if (!CollectionUtils.isEmpty(mdcMap)) {
			MDC.setContextMap(mdcMap);
		}
		try {
			return task.call();
		} finally {
			if (!CollectionUtils.isEmpty(mdcMap)) {
				MDC.clear();
			}
		}
	}
}
