package com.jindi.infra.core.exception;

/**
 * rpc访问异常
 */
public class RpcAccessException extends RuntimeException {

	public RpcAccessException() {
		super();
	}

	public RpcAccessException(String message) {
		super(message);
	}

	public RpcAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcAccessException(Throwable cause) {
		super(cause);
	}

	protected RpcAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
