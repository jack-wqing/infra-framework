package com.jindi.infra.registry.model;

import lombok.Data;

@Data
public class Result<T> {

	private Boolean success;

	private String message;

	private T data;
}
