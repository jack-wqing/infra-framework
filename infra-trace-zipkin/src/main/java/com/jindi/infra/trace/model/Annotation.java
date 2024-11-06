package com.jindi.infra.trace.model;

/**
 * Associates an event that explains latency with a timestamp. Unlike log statements, annotations are often codes. Ex. \&quot;ws\&quot; for WireSend  Zipkin v1 core annotations such as \&quot;cs\&quot; and \&quot;sr\&quot; have been replaced with Span.Kind, which interprets timestamp and duration.
 */
public class Annotation {

    private Integer timestamp = null;

    private String value = null;

    public Annotation timestamp(Integer timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Epoch **microseconds** of this event.  For example, 1502787600000000 corresponds to 2017-08-15 09:00 UTC  This value should be set directly by instrumentation, using the most precise value possible. For example, gettimeofday or multiplying epoch millis by 1000.
     *
     * @return timestamp
     **/
    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public Annotation value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Usually a short tag indicating an event, like \&quot;error\&quot;  While possible to add larger data, such as garbage collection details, low cardinality event names both keep the size of spans down and also are easy to search against.
     *
     * @return value
     **/
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

