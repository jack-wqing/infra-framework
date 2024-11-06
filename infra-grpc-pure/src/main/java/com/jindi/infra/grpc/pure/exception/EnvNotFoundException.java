package com.jindi.infra.grpc.pure.exception;

public class EnvNotFoundException extends Exception {

	public EnvNotFoundException() {
		super();
	}

	public EnvNotFoundException(String message) {
		super(message);
	}

	public EnvNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnvNotFoundException(Throwable cause) {
		super(cause);
	}

	protected EnvNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
