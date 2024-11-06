package com.jindi.infra.tools.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class TycExecutors {

	/**
	 * 创建线程池
	 *
	 * @param name
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param timeUnit
	 * @param blockingQueue
	 * @param rejectedExecutionHandler
	 * @return
	 */
	public static TycThreadPoolExecutor newTycThreadPoolExecutor(String name, Integer corePoolSize,
			Integer maximumPoolSize, Long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue,
			RejectedExecutionHandler rejectedExecutionHandler) {
		return new TycThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, blockingQueue,
				new ThreadFactory() {
					private final AtomicInteger counter = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format("thread-%s-%d", name, counter.getAndIncrement()));
					}
				}, rejectedExecutionHandler);
	}

}
