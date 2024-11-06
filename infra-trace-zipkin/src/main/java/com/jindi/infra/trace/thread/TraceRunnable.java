package com.jindi.infra.trace.thread;

import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 增强Runnable，支持MDC值在线程池传递
 *
 * @author changbo
 * @date 2021/8/9
 */
public class TraceRunnable implements Runnable {

	private Map<String, String> mdcMap;
	private Runnable task;

	private TraceRunnable(Runnable task, Map<String, String> mdcMap) {
		this.task = task;
		this.mdcMap = mdcMap;
	}

	public static Runnable wrap(Runnable task) {
		Objects.requireNonNull(task);
		return new TraceRunnable(task, MDC.getCopyOfContextMap());
	}

	@Override
	public void run() {
		if (!CollectionUtils.isEmpty(mdcMap)) {
			MDC.setContextMap(mdcMap);
		}
		try {
			task.run();
		} finally {
			if (!CollectionUtils.isEmpty(mdcMap)) {
				MDC.clear();
			}
		}
	}
}
