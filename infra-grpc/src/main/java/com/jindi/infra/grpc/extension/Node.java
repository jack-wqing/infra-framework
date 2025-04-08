package com.jindi.infra.grpc.extension;

import java.util.Objects;


public class Node {

	private String host;

	private Integer port;

	public Node() {
	}

	public Node(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Node node = (Node) o;
		return host.equals(node.host) && port.equals(node.port);
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port);
	}
}
