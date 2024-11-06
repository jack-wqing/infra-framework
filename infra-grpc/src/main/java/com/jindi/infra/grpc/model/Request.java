package com.jindi.infra.grpc.model;

import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.Data;

@Data
public class Request {

	private String application;
	private Boolean requestType; // request with/without return value
	private ParamType[] paramTypes;
	private String interfaceName;
	private String methodName;

	@Override
	public String toString() {
		return InnerJSONUtils.toJSONString(this);
	}
}
