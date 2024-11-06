package com.jindi.infra.tools.thread;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;


@Slf4j
public class MdcWrapper implements Runnable {

    private final Map<String, String> mdcMap;
    private final Runnable runnableTask;
    private final Long parentThreadId;

    private MdcWrapper(Runnable runnableTask, Map<String, String> mdcMap) {
        this.runnableTask = runnableTask;
        this.mdcMap = mdcMap;
        this.parentThreadId = Thread.currentThread().getId();

    }

    public static Runnable wrap(Runnable task) {
        Objects.requireNonNull(task);
        return new MdcWrapper(task, MDC.getCopyOfContextMap());
    }

    @Override
    public void run() {
        if (!CollectionUtils.isEmpty(mdcMap)) {
            MDC.setContextMap(mdcMap);
        }

        try {
            runnableTask.run();
        } catch (Throwable e) {
            throw e;
        } finally {
            Long currentThreadId = Thread.currentThread().getId();
            if (parentThreadId != null && !parentThreadId.equals(currentThreadId) && !CollectionUtils.isEmpty(mdcMap)) {
                MDC.clear();
            }
        }
    }
}
