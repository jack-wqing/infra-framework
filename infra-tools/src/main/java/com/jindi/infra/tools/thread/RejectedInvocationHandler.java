package com.jindi.infra.tools.thread;

import com.dianping.cat.Cat;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedInvocationHandler implements RejectedExecutionHandler {

    private RejectedExecutionHandler delegate;

    public RejectedInvocationHandler (RejectedExecutionHandler handler){
        this.delegate = handler;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor instanceof EnhanceThreadPoolExecutor) {
            Cat.logEvent("线程池拒绝", ((EnhanceThreadPoolExecutor) executor).getCurrentPoolName());
            ((EnhanceThreadPoolExecutor) executor).incRejectCount(1);
        }
        delegate.rejectedExecution(r, executor);
    }
}
