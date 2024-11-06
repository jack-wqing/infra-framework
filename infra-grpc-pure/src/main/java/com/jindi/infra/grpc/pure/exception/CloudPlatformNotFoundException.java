package com.jindi.infra.grpc.pure.exception;

public class CloudPlatformNotFoundException extends Exception {

	public CloudPlatformNotFoundException() {
		super();
	}

	public CloudPlatformNotFoundException(String message) {
		super(message);
	}

	public CloudPlatformNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public CloudPlatformNotFoundException(Throwable cause) {
		super(cause);
	}

	protected CloudPlatformNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
