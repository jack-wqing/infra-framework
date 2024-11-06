package com.jindi.infra.core.constants;

import java.util.Objects;

public enum Status {
	UNKNOWN(0, "未知状态"), OK(1, "成功"), INVALID_ARGUMENT(2, "无效的参数"), UNIMPLEMENTED(3, "接口没有实现"), SERVER_INTERNAL_ERROR(4,
			"服务端内部异常"), RESOURCE_EXHAUSTED(5, "资源超限");

	private Integer code;
	private String message;

	Status(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static Status valueOf(Integer code) {
		for (Status status : values()) {
			if (Objects.equals(status.getCode(), code)) {
				return status;
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
