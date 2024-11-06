package com.jindi.infra.trace.grpc.adapter;

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;

public class GrpcHeadersInjectAdapter implements TextMap {

	private Map<String, String> grpcHeaders;

	public GrpcHeadersInjectAdapter(Map<String, String> grpcHeaders) {
		this.grpcHeaders = grpcHeaders;
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return null;
	}

	@Override
	public void put(String key, String value) {
		grpcHeaders.put(key, value);
	}
}
