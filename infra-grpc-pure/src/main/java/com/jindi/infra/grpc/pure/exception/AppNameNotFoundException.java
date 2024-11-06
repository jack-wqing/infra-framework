package com.jindi.infra.grpc.pure.exception;

public class AppNameNotFoundException extends Exception {

	public AppNameNotFoundException() {
		super();
	}

	public AppNameNotFoundException(String message) {
		super(message);
	}

	public AppNameNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppNameNotFoundException(Throwable cause) {
		super(cause);
	}

	protected AppNameNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
