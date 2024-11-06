package com.jindi.infra.traffic.sentinel.cluster.selector;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import java.util.List;

public abstract class AbstractSelector<T> implements Selector<T> {

    @Override
    public T select(List<T> source) {
        if (CollectionUtils.isEmpty(source)) {
            return null;
        }
        if (source.size() == 1) {
            return source.get(0);
        }
        return doSelect(source);
    }

    protected abstract T doSelect(List<T> source);

}

