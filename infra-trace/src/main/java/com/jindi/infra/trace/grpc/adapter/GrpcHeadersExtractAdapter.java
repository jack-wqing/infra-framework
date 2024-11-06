package com.jindi.infra.trace.grpc.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.jindi.infra.grpc.util.GrpcHeaderUtils;

import io.grpc.Metadata;
import io.opentracing.propagation.TextMap;

public class GrpcHeadersExtractAdapter implements TextMap {

	private Map<String, String> headers = new HashMap<>();

	public GrpcHeadersExtractAdapter(Metadata metadata) {
		if (metadata != null && metadata.keys().size() > 0) {
			Iterator<String> it = metadata.keys().iterator();
			while (it.hasNext()) {
				String key = it.next();
				headers.put(key, GrpcHeaderUtils.getHeaderValue(key, metadata));
			}
		}
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return headers.entrySet().iterator();
	}

	@Override
	public void put(String key, String value) {
	}
}
