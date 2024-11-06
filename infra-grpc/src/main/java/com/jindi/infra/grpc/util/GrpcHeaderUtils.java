package com.jindi.infra.grpc.util;

import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.grpc.Metadata;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcHeaderUtils {

	private static Cache<String, Metadata.Key> cached = CacheBuilder.newBuilder().maximumSize(10000).build();

	public static Metadata.Key<String> getMetadataKeyOrCreate(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			return cached.get(key, () -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
		} catch (ExecutionException e) {
			log.error("getMetadataKeyOrCreate", e);
		}
		return null;
	}

	public static String getHeaderValue(String key, Metadata headers) {
		if (headers == null || StringUtils.isBlank(key)) {
			return null;
		}
		Metadata.Key<String> metadataKey = getMetadataKeyOrCreate(key);
		if (metadataKey == null) {
			return null;
		}
		return headers.get(metadataKey);
	}
}
