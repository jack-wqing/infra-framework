package com.jindi.infra.core.exception;

import com.jindi.infra.core.constants.Status;

/**
 * Rpc异常父类
 */
public class RpcException extends Exception {

	private Status status;

	public RpcException() {
	}

	public RpcException(String message) {
		super(message);
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(Throwable cause) {
		super(cause);
	}

	public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RpcException(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return this.status;
	}
}
