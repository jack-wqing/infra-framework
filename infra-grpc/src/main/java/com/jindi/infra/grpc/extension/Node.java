package com.jindi.infra.grpc.extension;

import java.util.Objects;

/**
 * 节点
 */
public class Node {

	/**
	 * 主机
	 */
	private String host;

	/**
	 * 端口
	 */
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
