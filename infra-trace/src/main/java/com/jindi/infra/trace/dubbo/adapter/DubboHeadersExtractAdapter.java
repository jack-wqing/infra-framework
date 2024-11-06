package com.jindi.infra.trace.dubbo.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;

public class DubboHeadersExtractAdapter implements TextMap {

	private Map<String, String> headers = new HashMap<>();

	public DubboHeadersExtractAdapter(Map<String, String> headers) {
		this.headers = headers;
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return headers.entrySet().iterator();
	}

	@Override
	public void put(String key, String value) {
	}
}
