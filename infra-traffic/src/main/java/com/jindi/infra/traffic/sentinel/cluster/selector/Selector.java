package com.jindi.infra.traffic.sentinel.cluster.selector;


import java.util.List;

/**
 * @param <T> T
 */
public interface Selector<T> {

    T select(List<T> source);
}
