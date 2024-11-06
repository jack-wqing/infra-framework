package com.jindi.infra.core.constants;

import java.io.*;

import io.grpc.Metadata;

public class GrpcConsts {

	public static final String EXCEPTION_STACK_TRACE = "exception_stack_trace-bin";
	public static final String EXCEPTION_CODE = "exception_code-bin";

	private static final String GRPC_REQUEST_HEADER_SERIALIZATION_EXCEPTION = "grpc请求头序列化异常";
	private static final Metadata.BinaryMarshaller MARSHALLER = new Metadata.BinaryMarshaller() {

		@Override
		public byte[] toBytes(Object value) {
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeObject(value);
				objectOutputStream.flush();
				return byteArrayOutputStream.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException(GRPC_REQUEST_HEADER_SERIALIZATION_EXCEPTION, e);
			}
		}

		@Override
		public Object parseBytes(byte[] serialized) {
			try {
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialized);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				return objectInputStream.readObject();
			} catch (Exception e) {
				throw new RuntimeException(GRPC_REQUEST_HEADER_SERIALIZATION_EXCEPTION, e);
			}
		}
	};

	public static final Metadata.Key<String> EXCEPTION_STACK_TRACE_KEY = Metadata.Key.of(EXCEPTION_STACK_TRACE,
			MARSHALLER);
	public static final Metadata.Key<String> EXCEPTION_CODE_KEY = Metadata.Key.of(EXCEPTION_CODE, MARSHALLER);
}
