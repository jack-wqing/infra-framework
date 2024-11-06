package com.jindi.infra.core.exception;

import com.jindi.infra.core.constants.Status;

/**
 * rpc服务端异常
 */
public class RpcServerException extends RpcException {

	public RpcServerException() {
		super();
	}

	public RpcServerException(String message) {
		super(message);
	}

	public RpcServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcServerException(Throwable cause) {
		super(cause);
	}

	public RpcServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RpcServerException(Status status) {
		super(status);
	}
}
