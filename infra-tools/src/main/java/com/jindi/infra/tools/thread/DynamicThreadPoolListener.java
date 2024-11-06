package com.jindi.infra.tools.thread;


import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicThreadPoolListener {

    private static final String PREFIX = "infra.thread.pool.";

    //通过反射调用refresh完成线程变更
    public void refresh(ConfigChangeEvent changeEvent) {
        Map<String, String> properties = getProperty(changeEvent);
        refreshInternal(properties);
    }

    public void refreshInternal(Map<String, String> properties) {
        List<ThreadChangeOpt> threadModifyOptMap = getChangeOpt(properties);
        if (CollectionUtils.isEmpty(threadModifyOptMap)) {
            return;
        }
        for (ThreadChangeOpt changeOpt : threadModifyOptMap) {
            refreshThreadPoolExecutor(changeOpt);
        }
    }

    private Map<String, String> getProperty(ConfigChangeEvent changeEvent) {
        Map<String, String> result = new HashMap<>();
        for (String changedKey : changeEvent.changedKeys()) {
            if (!changedKey.contains(PREFIX)) {
                continue;
            }
            result.put(changedKey.substring(changedKey.indexOf(PREFIX) + PREFIX.length()), changeEvent.getChange(changedKey).getNewValue());
        }
        return result;
    }



    private void refreshThreadPoolExecutor(ThreadChangeOpt changeOpt) {
        EnhanceThreadPoolExecutor enhanceThreadPoolExecutor = EnhanceThreadPoolExecutor.getExistThreadPoolExecutorList()
                .stream().collect(Collectors.toMap(EnhanceThreadPoolExecutor::getCurrentPoolName, Function.identity()))
                .get(changeOpt.getThreadName());
        if (changeOpt.getCorePoolSize() != null) {
            enhanceThreadPoolExecutor.setCorePoolSize(changeOpt.getCorePoolSize());
        }
        if (changeOpt.getMaxPoolSize() != null) {
            enhanceThreadPoolExecutor.setMaximumPoolSize(changeOpt.getMaxPoolSize());
        }
    }

    private List<ThreadChangeOpt> getChangeOpt(Map<String, String> properties) {
        Set<String> changedKeys = properties.keySet();
        Map<String, ThreadChangeOpt> result = new HashMap<>();
        Set<String> existThreadPool = EnhanceThreadPoolExecutor.getExistThreadPoolExecutorList().stream().map(EnhanceThreadPoolExecutor::getCurrentPoolName).collect(Collectors.toSet());
        for (String changedKey : changedKeys) {
            String threadName = getThreadName(changedKey);
            if (StringUtils.isBlank(threadName) || !existThreadPool.contains(threadName)) {
                continue;
            }
            ThreadChangeOpt threadChangeOpt = getOrInit(result, threadName);
            if (changedKey.endsWith("corePoolSize")) {
                threadChangeOpt.setCorePoolSize(Integer.valueOf(properties.get(changedKey)));
            } else if (changedKey.endsWith("maxPoolSize")) {
                threadChangeOpt.setMaxPoolSize(Integer.valueOf(properties.get(changedKey)));
            }
        }
        return new ArrayList<>(result.values());
    }

    private ThreadChangeOpt getOrInit(Map<String, ThreadChangeOpt> result, String threadName) {
        ThreadChangeOpt threadChangeOpt = result.get(threadName);
        if (threadChangeOpt == null) {
            threadChangeOpt = new ThreadChangeOpt();
            threadChangeOpt.threadName = threadName;
            result.put(threadName, threadChangeOpt);
        }
        return threadChangeOpt;
    }

    private String getThreadName(String changedKey) {
        int index = changedKey.indexOf(".");
        if (index > 0) {
            return changedKey.substring(0, index);
        }
        return null;
    }

    @Data
    private static class ThreadChangeOpt {
        private String threadName;
        private Integer corePoolSize;
        private Integer maxPoolSize;
    }
}
