package com.jindi.infra.mdc;

import com.jindi.infra.tools.mdc.MDCRunnable;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MDCRunnableTests {

	private static final String KEY = "name";
	private static final String VALUE = "CaoXin";

	@Test
	public void testWrap() throws InterruptedException {
		MDC.put(KEY, VALUE);
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		CountDownLatch countDownLatch = new CountDownLatch(1);
		executorService.submit(
				MDCRunnable.wrap(
						() -> {
							System.out.println(MDC.get(KEY));
							assert Objects.equals(MDC.get(KEY), VALUE) : "MDC传播有问题";
							countDownLatch.countDown();
						}));
		countDownLatch.await();
	}
}
