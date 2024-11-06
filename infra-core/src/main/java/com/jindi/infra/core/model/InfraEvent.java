package com.jindi.infra.core.model;

import com.jindi.infra.core.constants.EventType;

import lombok.Data;

import java.util.List;

@Data
public class InfraEvent {

	/**
	 * 业务组
	 */
	private String group;

	/**
	 * 应用名
	 */
	private String appName;

	/**
	 * 事件类型
	 */
	private EventType eventType;

	/**
	 * 主机IP
	 */
	private String ip;

	/**
	 * 监听端口
	 */
	private Integer port;

	/**
	 * 数据
	 */
	private String data;

	/**
	 * http method
	 */
	private List<String> httpMethods;
}
