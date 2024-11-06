package com.jindi.infra.tools.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import lombok.extern.slf4j.Slf4j;

/**
 * 本地缓存工具类
 */
@Slf4j
public class LocalCache<K, V> {

	private Cache<K, V> cache;

	private LocalCache() {
	}

	private LocalCache(Cache<K, V> cache) {
		this.cache = cache;
	}

	public static <K, V> LocalCache<K, V> create(Integer maximumSize, Integer milliseconds) {
		Cache cache = CacheBuilder.newBuilder().maximumSize(maximumSize)
				.expireAfterWrite(milliseconds, TimeUnit.MILLISECONDS)
				.removalListener(new RemovalListener<Object, Object>() {
					@Override
					public void onRemoval(RemovalNotification<Object, Object> removalNotification) {
						log.debug("local cache remove notification key = {} value = {}", removalNotification.getKey(),
								removalNotification.getValue());
					}
				}).build();
		return new LocalCache<>(cache);
	}

	/**
	 * 从本地缓存获取，不存在时返回null
	 *
	 * @param cache
	 *            本地缓存
	 * @param k
	 *            键
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	public V get(K k) {
		return cache.getIfPresent(k);
	}

	/**
	 * 从本地缓存获取，不存在时通过loader加载
	 * @return
	 */
	public V get(K k, Callable<? extends V> loader) throws ExecutionException {
		return cache.get(k, loader);
	}

	/**
	 * 缓存键值对
	 * @param k 键
	 * @param v 值
	 */
	public void put(K k, V v) {
		cache.put(k, v);
	}

	/**
	 * 从本地缓存删除key对应的value值
	 *
	 * @param cache
	 *            本地缓存
	 * @param k
	 *            键
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	public void delete(K k) {
		cache.invalidate(k);
	}
}
