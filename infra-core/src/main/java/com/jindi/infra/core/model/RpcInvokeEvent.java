package com.jindi.infra.core.model;

import org.springframework.context.ApplicationEvent;

import com.jindi.infra.core.constants.EventType;
import com.jindi.infra.core.constants.MethodType;

public class RpcInvokeEvent extends ApplicationEvent {

	private String service;
	private String method;
	private MethodType methodType;
	private EventType eventType;

	public RpcInvokeEvent(Object source, String service, String method, MethodType methodType, EventType eventType) {
		super(source);
		this.service = service;
		this.method = method;
		this.methodType = methodType;
		this.eventType = eventType;
	}

	/**
	 * Create a new {@code ApplicationEvent}.
	 *
	 * @param source
	 *            the object on which the event initially occurred or with which the
	 *            event is associated (never {@code null})
	 */
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public MethodType getMethodType() {
		return methodType;
	}

	public void setMethodType(MethodType methodType) {
		this.methodType = methodType;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
}
