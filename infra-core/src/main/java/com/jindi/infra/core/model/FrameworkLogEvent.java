package com.jindi.infra.core.model;

import org.springframework.context.ApplicationEvent;

public class FrameworkLogEvent extends ApplicationEvent {

    private String type;
    private String name;
    private String message;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public FrameworkLogEvent(Object source) {
        super(source);
    }

    public FrameworkLogEvent(Object source, String type, String name, String message) {
        super(source);
        this.type = type;
        this.name = name;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
