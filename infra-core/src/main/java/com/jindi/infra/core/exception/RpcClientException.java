package com.jindi.infra.core.exception;

import com.jindi.infra.core.constants.Status;

/**
 * rpc客户端异常
 */
public class RpcClientException extends RpcException {

	public RpcClientException() {
		super();
	}

	public RpcClientException(String message) {
		super(message);
	}

	public RpcClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcClientException(Throwable cause) {
		super(cause);
	}

	public RpcClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RpcClientException(Status status) {
		super(status);
	}
}
