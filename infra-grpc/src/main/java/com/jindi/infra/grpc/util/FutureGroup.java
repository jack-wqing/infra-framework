package com.jindi.infra.grpc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import org.springframework.util.CollectionUtils;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessageV3;
import com.jindi.infra.core.util.PbUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureGroup<R extends GeneratedMessageV3> {

	private static final ExecutorService executorService = Executors.newFixedThreadPool(200);
	private static final String DEFAULT = "default";
	private List<AsyncFunction<R>> asyncFunctions;
	private String name;

	private FutureGroup(String name) {
		this.name = name;
	}

	public static <T extends GeneratedMessageV3> FutureGroup create(String name) {
		return new FutureGroup<T>(name);
	}

	public static <T extends GeneratedMessageV3> FutureGroup create() {
		return new FutureGroup<T>(DEFAULT);
	}

	public void submit(AsyncFunction<R> asyncFunction) {
		Objects.requireNonNull(asyncFunction, "asyncFunction 必须非NULL");
		if (asyncFunctions == null) {
			asyncFunctions = new ArrayList<>();
		}
		asyncFunctions.add(asyncFunction);
	}

	public List<R> blockingGet(Integer timeoutMS) throws InterruptedException {
		if (CollectionUtils.isEmpty(asyncFunctions)) {
			return Collections.emptyList();
		}
		CountDownLatch countDownLatch = new CountDownLatch(asyncFunctions.size());
		List<ListenableFuture<R>> listenableFutures = new ArrayList<>(asyncFunctions.size());
		asyncInvoke(countDownLatch, listenableFutures);
		countDownLatch.await(timeoutMS, TimeUnit.MILLISECONDS);
		List<R> values = new ArrayList<>(listenableFutures.size());
		for (int i = 0; i < listenableFutures.size(); i++) {
			ListenableFuture<R> listenableFuture = listenableFutures.get(i);
			if (listenableFuture.isDone()) {
				try {
					R value = listenableFuture.get();
					log.debug("futureGroup name = {}, i = {}, value = {}", name, i, PbUtils.toJSONString(value));
					values.add(value);
				} catch (ExecutionException e) {
					log.error("", GrpcUtils.parseCause(e));
				}
				continue;
			}
			log.info("futureGroup name = {}, i = {} 请求执行超时 timeoutMS = {}毫秒", name, i, timeoutMS);
			values.add(null);
		}
		return values;
	}

	private void asyncInvoke(CountDownLatch countDownLatch, List<ListenableFuture<R>> listenableFutures) {
		for (AsyncFunction<R> asyncFunction : asyncFunctions) {
			ListenableFuture<R> listenableFuture = null;
			try {
				listenableFuture = asyncFunction.get();
			} catch (Throwable e) {
				countDownLatch.countDown();
				listenableFutures.add(Futures.immediateFailedFuture(e));
				continue;
			}
			listenableFuture.addListener(countDownLatch::countDown, executorService);
			listenableFutures.add(listenableFuture);
		}
	}
}
