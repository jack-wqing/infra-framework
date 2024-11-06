package com.jindi.infra.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.jindi.infra.tools.concurrent.TycExecutors;
import com.jindi.infra.tools.concurrent.TycThreadLocal;
import com.jindi.infra.tools.concurrent.TycThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TycThreadLocalTests {

	private static final TycThreadLocal<Integer> ID_THREAD_LOCAL = new TycThreadLocal();

	@Test
	public void testGet() throws InterruptedException {
		TycThreadPoolExecutor threadPoolExecutor = TycExecutors.newTycThreadPoolExecutor("test", 4, 4, 0L,
				TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
		for (int i = 0; i < 10000; i++) {
			ID_THREAD_LOCAL.set(i);
			threadPoolExecutor.execute(() -> log.info("thread local id = {}", ID_THREAD_LOCAL.get()));
		}
		Thread.sleep(5000);
	}
}
