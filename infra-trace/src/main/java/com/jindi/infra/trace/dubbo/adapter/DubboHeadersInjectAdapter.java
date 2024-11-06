package com.jindi.infra.trace.dubbo.adapter;

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;

public class DubboHeadersInjectAdapter implements TextMap {

	private Map<String, String> headers;

	public DubboHeadersInjectAdapter(Map<String, String> headers) {
		this.headers = headers;
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return null;
	}

	@Override
	public void put(String key, String value) {
		headers.put(key, value);
	}
}
