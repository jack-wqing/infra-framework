package com.jindi.infra.grpc.model;

import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> {

	private final Long createTime = System.currentTimeMillis();
	private T data;
	private Integer code;
	private String message;

	@Override
	public String toString() {
		return InnerJSONUtils.toJSONString(this);
	}
}
