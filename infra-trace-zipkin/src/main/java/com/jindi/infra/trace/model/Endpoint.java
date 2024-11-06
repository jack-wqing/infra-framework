package com.jindi.infra.trace.model;

/**
 * The network context of a node in the service graph
 */
public class Endpoint {

    private String serviceName = null;

    private String ipv4 = null;

    private String ipv6 = null;

    private Integer port = null;

    public Endpoint serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Lower-case label of this node in the service graph, such as \&quot;favstar\&quot;. Leave absent if unknown.  This is a primary label for trace lookup and aggregation, so it should be intuitive and consistent. Many use a name from service discovery.
     *
     * @return serviceName
     **/
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Endpoint ipv4(String ipv4) {
        this.ipv4 = ipv4;
        return this;
    }

    /**
     * The text representation of the primary IPv4 address associated with this connection. Ex. 192.168.99.100 Absent if unknown.
     *
     * @return ipv4
     **/
    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Endpoint ipv6(String ipv6) {
        this.ipv6 = ipv6;
        return this;
    }

    /**
     * The text representation of the primary IPv6 address associated with a connection. Ex. 2001:db8::c001 Absent if unknown.  Prefer using the ipv4 field for mapped addresses.
     *
     * @return ipv6
     **/
    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public Endpoint port(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Depending on context, this could be a listen port or the client-side of a socket. Absent if unknown. Please don&#39;t set to zero.
     *
     * @return port
     **/
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}

