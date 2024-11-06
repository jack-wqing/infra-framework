package com.jindi.infra.core.exception;

import com.jindi.infra.core.constants.Status;

public class RpcBlockException extends RpcException {

	public RpcBlockException() {
		super();
	}

	public RpcBlockException(String message) {
		super(message);
	}

	public RpcBlockException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcBlockException(Throwable cause) {
		super(cause);
	}

	public RpcBlockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RpcBlockException(Status status) {
		super(status);
	}
}
