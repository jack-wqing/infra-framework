package com.jindi.infra.grpc.model;

import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.Data;

@Data
public class ParamType {

	private String name;
	private String type;
	private Object sample;

	@Override
	public String toString() {
		return InnerJSONUtils.toJSONString(this);
	}
}
