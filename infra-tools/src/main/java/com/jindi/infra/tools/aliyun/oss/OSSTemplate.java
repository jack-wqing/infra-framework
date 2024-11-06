package com.jindi.infra.tools.aliyun.oss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;

public class OSSTemplate {

	@Autowired
	private OSS oss;

	public byte[] getByteArray(String bucket, String key) throws IOException {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		return IOUtils.readStreamAsByteArray(getObjectContent(bucket, key));
	}

	public String getString(String bucket, String key) throws IOException {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		return IOUtils.readStreamAsString(getObjectContent(bucket, key), "UTF-8");
	}

	public InputStream getObjectContent(String bucket, String key) {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		OSSObject ossObject = getObject(bucket, key);
		return ossObject.getObjectContent();
	}

	public OSSObject getObject(String bucket, String key) {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		return oss.getObject(bucket, key);
	}

	public PutObjectResult put(String bucket, String key, InputStream inputStream) {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		Objects.requireNonNull(inputStream, "inputStream require non null");
		return oss.putObject(bucket, key, inputStream);
	}

	public PutObjectResult putByteArray(String bucket, String key, byte[] value) {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		Objects.requireNonNull(value, "value require non null");
		return oss.putObject(bucket, key, new ByteArrayInputStream(value));
	}

	public PutObjectResult putString(String bucket, String key, String value) {
		Objects.requireNonNull(bucket, "bucket require non null");
		Objects.requireNonNull(key, "key require non null");
		Objects.requireNonNull(value, "value require non null");
		return putByteArray(bucket, key, value.getBytes(StandardCharsets.UTF_8));
	}
}
