package com.jindi.infra.grpc.constant;

import com.jindi.infra.common.util.InnerJSONUtils;

public enum ResponseCode {
	SUCCESS(1, "成功"), NOT_FOUND(2, "没有找到匹配的方法"), INPUT_PARAMETER_ERROR(3, "输入参数错误"), SERVICE_ACCESS_EXCEPTION(4,
			"服务访问异常"), INVOKE_EXCEPTION(5, "服务调用异常 exception: %s");
	private final Integer code;
	private final String message;

	ResponseCode(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return InnerJSONUtils.toJSONString(this);
	}
}
