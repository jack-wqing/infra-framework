package com.jindi.infra.thread;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jindi.infra.tools.thread.EnhanceThreadPoolExecutor;
import com.jindi.infra.tools.thread.RejectedInvocationHandler;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TycExecutorTests {

    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String THREAD_NAME = "testThreadPool";

    @Test
    public void testExecute() {
        MDC.put(KEY, VALUE);
        EnhanceThreadPoolExecutor tycExecutor = new EnhanceThreadPoolExecutor(THREAD_NAME, 1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));
        tycExecutor.execute(
                        () -> {
                            System.out.println(MDC.get(KEY));
                            assert Objects.equals(MDC.get(KEY), VALUE) : "MDC传播有问题";
                            assert Thread.currentThread().getName().contains(THREAD_NAME) : "线程池名字出现异常";
                        });
        assert tycExecutor.getCurrentPoolName().contains(THREAD_NAME) : "线程池名字出现异常";
    }

    @Test
    public void testSubmitRunnable() throws ExecutionException, InterruptedException {
        MDC.put(KEY, VALUE);
        EnhanceThreadPoolExecutor tycExecutor = new EnhanceThreadPoolExecutor(THREAD_NAME, 1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));
        Future future = tycExecutor.submit(
                () -> {
                    System.out.println(MDC.get(KEY));
                    assert Objects.equals(MDC.get(KEY), VALUE) : "MDC传播有问题";
                    assert Thread.currentThread().getName().contains(THREAD_NAME) : "线程池名字出现异常";
                });
        future.get();
        assert tycExecutor.getCurrentPoolName().contains(THREAD_NAME) : "线程池名字出现异常";
    }

    @Test
    public void testSubmitCallable() throws ExecutionException, InterruptedException {
        MDC.put(KEY, VALUE);
        EnhanceThreadPoolExecutor tycExecutor = new EnhanceThreadPoolExecutor(THREAD_NAME, 1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));
        Future<String> future = tycExecutor.submit(
                () -> {
                    System.out.println(MDC.get(KEY));
                    assert Objects.equals(MDC.get(KEY), VALUE) : "MDC传播有问题";
                    assert Thread.currentThread().getName().contains(THREAD_NAME) : "线程名字出现异常";
                    return MDC.get(KEY);
                });
        future.get();
        assert tycExecutor.getCurrentPoolName().contains(THREAD_NAME) : "线程池名字出现异常";
    }

    @Test
    public void testReject() throws ExecutionException, InterruptedException {
        MDC.put(KEY, VALUE);
        EnhanceThreadPoolExecutor tycExecutor = new EnhanceThreadPoolExecutor(THREAD_NAME, 1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());
        addJob(tycExecutor);
        addJob(tycExecutor);
        addJob(tycExecutor);
        assert Objects.equals(MDC.get(KEY), VALUE) : "MDC传播有问题";
        assert tycExecutor.getCurrentPoolName().contains(THREAD_NAME) : "线程池名字出现异常";
    }

    @Test
    public void testAllConstructor() throws ExecutionException, InterruptedException {
        EnhanceThreadPoolExecutor threadPoolExecutor = new EnhanceThreadPoolExecutor(10, 10, 10L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        EnhanceThreadPoolExecutor threadPoolExecutor2 = new EnhanceThreadPoolExecutor(10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.AbortPolicy());
        EnhanceThreadPoolExecutor threadPoolExecutor3 = new EnhanceThreadPoolExecutor(10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), getThreadFactory("test"));
        EnhanceThreadPoolExecutor threadPoolExecutor4 = new EnhanceThreadPoolExecutor(10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), getThreadFactory("test"), new ThreadPoolExecutor.AbortPolicy());
        EnhanceThreadPoolExecutor threadPoolExecutor5 = new EnhanceThreadPoolExecutor("test1", 10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        EnhanceThreadPoolExecutor threadPoolExecutor6 = new EnhanceThreadPoolExecutor("test2", 10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.AbortPolicy());
        EnhanceThreadPoolExecutor threadPoolExecutor7 = new EnhanceThreadPoolExecutor("test3", 10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), getThreadFactory("test"));
        EnhanceThreadPoolExecutor threadPoolExecutor8 = new EnhanceThreadPoolExecutor("test4", 10,10,10L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), getThreadFactory("test"), new ThreadPoolExecutor.AbortPolicy());
        addJob(threadPoolExecutor);
        addJob(threadPoolExecutor2);
        addJob(threadPoolExecutor3);
        addJob(threadPoolExecutor4);
        addJob(threadPoolExecutor5);
        addJob(threadPoolExecutor6);
        addJob(threadPoolExecutor7);
        addJob(threadPoolExecutor8);
    }

    private void addJob(EnhanceThreadPoolExecutor tycExecutor) {
        tycExecutor.execute(
                () -> {
                    try {
                        Thread.sleep(100L);
                    } catch (Exception e) {
                        //do nothing
                    }
                });
    }

    public static ThreadFactory getThreadFactory(String nameFormat) {
        return (new ThreadFactoryBuilder()).setNameFormat(nameFormat).build();
    }

}
