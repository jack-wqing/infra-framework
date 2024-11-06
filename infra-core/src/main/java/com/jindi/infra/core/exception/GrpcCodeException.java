package com.jindi.infra.core.exception;

public class GrpcCodeException extends RuntimeException {

	private Integer code;

	public GrpcCodeException(Integer code) {
		super();
		this.code = code;
	}

	public GrpcCodeException(Integer code, String message) {
		super(message);
		this.code = code;
	}

	public GrpcCodeException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public GrpcCodeException(Integer code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public Integer getCode() {
		return this.code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
}
