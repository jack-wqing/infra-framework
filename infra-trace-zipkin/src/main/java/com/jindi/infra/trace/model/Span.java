package com.jindi.infra.trace.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A span is a single-host view of an operation. A trace is a series of spans (often RPC calls) which nest to form a latency tree. Spans are in the same trace when they share the same trace ID. The parent_id field establishes the position of one span in the tree.  The root span is where parent_id is Absent and usually has the longest duration in the trace. However, nested asynchronous work can materialize as child spans whose duration exceed the root span.  Spans usually represent remote activity such as RPC calls, or messaging producers and consumers. However, they can also represent in-process activity in any position of the trace. For example, a root span could represent a server receiving an initial client request. A root span could also represent a scheduled job that has no remote context.
 */
public class Span {

    private String traceId = null;
    private String name = null;
    private String parentId = null;
    private String id = null;
    private KindEnum kind = null;
    private Long timestamp = null;
    private Long timestamp_millis = null;
    private Long duration = null;
    private Boolean debug = null;
    private Boolean shared = null;
    private Endpoint localEndpoint = null;
    private Endpoint remoteEndpoint = null;
    private List<Annotation> annotations = null;
    private Tags tags = null;

    public Span traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * Randomly generated, unique identifier for a trace, set on all spans within it.  Encoded as 16 or 32 lowercase hex characters corresponding to 64 or 128 bits. For example, a 128bit trace ID looks like 4e441824ec2b6a44ffdc9bb9a6453df3
     *
     * @return traceId
     **/
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Span name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The logical operation this span represents in lowercase (e.g. rpc method). Leave absent if unknown.  As these are lookup labels, take care to ensure names are low cardinality. For example, do not embed variables into the name.
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Span parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * The parent span ID or absent if this the root span in a trace.
     *
     * @return parentId
     **/
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Span id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Unique 64bit identifier for this operation within the trace.  Encoded as 16 lowercase hex characters. For example ffdc9bb9a6453df3
     *
     * @return id
     **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Span kind(KindEnum kind) {
        this.kind = kind;
        return this;
    }

    /**
     * When present, kind clarifies timestamp, duration and remoteEndpoint. When absent, the span is local or incomplete. Unlike client and server, there is no direct critical path latency relationship between producer and consumer spans.  * &#x60;CLIENT&#x60;   * timestamp is the moment a request was sent to the server. (in v1 \&quot;cs\&quot;)   * duration is the delay until a response or an error was received. (in v1 \&quot;cr\&quot;-\&quot;cs\&quot;)   * remoteEndpoint is the server. (in v1 \&quot;sa\&quot;) * &#x60;SERVER&#x60;   * timestamp is the moment a client request was received. (in v1 \&quot;sr\&quot;)   * duration is the delay until a response was sent or an error. (in v1 \&quot;ss\&quot;-\&quot;sr\&quot;)   * remoteEndpoint is the client. (in v1 \&quot;ca\&quot;) * &#x60;PRODUCER&#x60;   * timestamp is the moment a message was sent to a destination. (in v1  \&quot;ms\&quot;)   * duration is the delay sending the message, such as batching.   * remoteEndpoint is the broker. * &#x60;CONSUMER&#x60;   * timestamp is the moment a message was received from an origin. (in v1 \&quot;mr\&quot;)   * duration is the delay consuming the message, such as from backlog.   * remoteEndpoint - Represents the broker. Leave serviceName absent if unknown.
     *
     * @return kind
     **/
    public KindEnum getKind() {
        return kind;
    }

    public void setKind(KindEnum kind) {
        this.kind = kind;
    }

    public Span timestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Epoch microseconds of the start of this span, possibly absent if incomplete.  For example, 1502787600000000 corresponds to 2017-08-15 09:00 UTC  This value should be set directly by instrumentation, using the most precise value possible. For example, gettimeofday or multiplying epoch millis by 1000.  There are three known edge-cases where this could be reported absent.  * A span was allocated but never started (ex not yet received a timestamp)  * The span&#39;s start event was lost  * Data about a completed span (ex tags) were sent after the fact
     *
     * @return timestamp
     **/
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Span duration(Long duration) {
        this.duration = duration;
        return this;
    }

    public Long getTimestamp_millis() {
        return timestamp_millis;
    }

    public void setTimestamp_millis(Long timestamp_millis) {
        this.timestamp_millis = timestamp_millis;
    }

    /**
     * Duration in **microseconds** of the critical path, if known. Durations of less than one are rounded up. Duration of children can be longer than their parents due to asynchronous operations.  For example 150 milliseconds is 150000 microseconds.
     * minimum: 1
     *
     * @return duration
     **/
    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Span debug(Boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * True is a request to store this span even if it overrides sampling policy.  This is true when the &#x60;X-B3-Flags&#x60; header has a value of 1.
     *
     * @return debug
     **/
    public Boolean isDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public Span shared(Boolean shared) {
        this.shared = shared;
        return this;
    }

    /**
     * True if we are contributing to a span started by another tracer (ex on a different host).
     *
     * @return shared
     **/
    public Boolean isShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Span localEndpoint(Endpoint localEndpoint) {
        this.localEndpoint = localEndpoint;
        return this;
    }

    /**
     * The host that recorded this span, primarily for query by service name.  Instrumentation should always record this. Usually, absent implies late data. The IP address corresponding to this is usually the site local or advertised service address. When present, the port indicates the listen port.
     *
     * @return localEndpoint
     **/
    public Endpoint getLocalEndpoint() {
        return localEndpoint;
    }

    public void setLocalEndpoint(Endpoint localEndpoint) {
        this.localEndpoint = localEndpoint;
    }

    public Span remoteEndpoint(Endpoint remoteEndpoint) {
        this.remoteEndpoint = remoteEndpoint;
        return this;
    }

    /**
     * When an RPC (or messaging) span, indicates the other side of the connection.  By recording the remote endpoint, your trace will contain network context even if the peer is not tracing. For example, you can record the IP from the &#x60;X-Forwarded-For&#x60; header or the service name and socket of a remote peer.
     *
     * @return remoteEndpoint
     **/
    public Endpoint getRemoteEndpoint() {
        return remoteEndpoint;
    }

    public void setRemoteEndpoint(Endpoint remoteEndpoint) {
        this.remoteEndpoint = remoteEndpoint;
    }

    public Span annotations(List<Annotation> annotations) {
        this.annotations = annotations;
        return this;
    }

    public Span addAnnotationsItem(Annotation annotationsItem) {
        if (this.annotations == null) {
            this.annotations = new ArrayList<Annotation>();
        }
        this.annotations.add(annotationsItem);
        return this;
    }

    /**
     * Associates events that explain latency with the time they happened.
     *
     * @return annotations
     **/
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public Span tags(Tags tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Tags give your span context for search, viewing and analysis.
     *
     * @return tags
     **/
    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public void tag(String key, String value) {
        if (tags == null) {
            tags = new Tags();
        }
        tags.put(key, value);
    }

    /**
     * When present, kind clarifies timestamp, duration and remoteEndpoint. When absent, the span is local or incomplete. Unlike client and server, there is no direct critical path latency relationship between producer and consumer spans.  * &#x60;CLIENT&#x60;   * timestamp is the moment a request was sent to the server. (in v1 \&quot;cs\&quot;)   * duration is the delay until a response or an error was received. (in v1 \&quot;cr\&quot;-\&quot;cs\&quot;)   * remoteEndpoint is the server. (in v1 \&quot;sa\&quot;) * &#x60;SERVER&#x60;   * timestamp is the moment a client request was received. (in v1 \&quot;sr\&quot;)   * duration is the delay until a response was sent or an error. (in v1 \&quot;ss\&quot;-\&quot;sr\&quot;)   * remoteEndpoint is the client. (in v1 \&quot;ca\&quot;) * &#x60;PRODUCER&#x60;   * timestamp is the moment a message was sent to a destination. (in v1  \&quot;ms\&quot;)   * duration is the delay sending the message, such as batching.   * remoteEndpoint is the broker. * &#x60;CONSUMER&#x60;   * timestamp is the moment a message was received from an origin. (in v1 \&quot;mr\&quot;)   * duration is the delay consuming the message, such as from backlog.   * remoteEndpoint - Represents the broker. Leave serviceName absent if unknown.
     */
    public enum KindEnum {
        CLIENT("CLIENT"),

        SERVER("SERVER"),

        PRODUCER("PRODUCER"),

        CONSUMER("CONSUMER");

        private String value;

        KindEnum(String value) {
            this.value = value;
        }

        public static KindEnum fromValue(String text) {
            for (KindEnum b : KindEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }


}

