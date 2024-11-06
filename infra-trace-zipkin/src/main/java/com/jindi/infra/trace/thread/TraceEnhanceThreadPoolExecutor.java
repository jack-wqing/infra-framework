package com.jindi.infra.trace.thread;

import java.util.concurrent.*;

/**
 * 增强线程池，支持MDC值在线程池传递
 *
 * @author changbo
 * @date 2021/8/4
 */
public class TraceEnhanceThreadPoolExecutor extends ThreadPoolExecutor {

	public TraceEnhanceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public TraceEnhanceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public TraceEnhanceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public TraceEnhanceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	@Override
	public void execute(Runnable command) {
		super.execute(TraceRunnable.wrap(command));
	}

	@Override
	public Future<?> submit(Runnable task) {
		return super.submit(TraceRunnable.wrap(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return super.submit(TraceRunnable.wrap(task), result);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(TraceCallable.wrap(task));
	}
}
