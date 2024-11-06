package com.jindi.infra.thread;


import com.jindi.infra.tools.thread.EnhanceThreadPoolExecutor;
import com.jindi.infra.tools.thread.PriorityBlockingQueue;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PriorityThreadPoolExecutorTests {

    @Test
    public void checkPriorityThreadPoolExecutor() {
        EnhanceThreadPoolExecutor executor = new EnhanceThreadPoolExecutor("test", 1, 2, 0, TimeUnit.SECONDS, new PriorityBlockingQueue<>(1));
        System.out.println(executor.getQueue().getClass().getSimpleName());
        addJob(executor, 1);
        addJob(executor, 2);
        assert executor.getQueueSize() == 0;
        addJob(executor, 3);
        assert executor.getQueueSize() == 1;
        try {
            addJob(executor, 4);
        } catch (Exception e) {
            System.out.println("拒绝异常");
            assert e.getMessage().contains("RejectedExecutionException");
        }
    }

    private static void addJob(EnhanceThreadPoolExecutor tycExecutor, Integer tag) {
        tycExecutor.execute(
                () -> {
                    try {
                        Long seconds = 60L;
                        while(seconds > 0L) {
                            System.out.println("tag: " + tag + " seconds: " + seconds);
                            seconds--;
                            Thread.sleep(1000L);
                        }
                    } catch (Exception e) {
                        //do nothing
                    }
                });

    }

}
