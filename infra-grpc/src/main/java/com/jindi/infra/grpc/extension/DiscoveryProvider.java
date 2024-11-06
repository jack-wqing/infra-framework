package com.jindi.infra.grpc.extension;

import java.util.List;

/**
 * 服务发现
 */
public interface DiscoveryProvider {

	String ENV = "env";
	String SERVER_PORT = "serverPort";
	String SERVICES = "services";
	String TAGS_KEY = "tags";
	String REGION = "region";
	String REGISTRATION_TIME = "registrationTime";

	/**
	 * 服务提供者注册
	 */
	void register() throws Exception;

	/**
	 * 服务提供者注销
	 *
	 * @throws Exception
	 */
	void unregister() throws Exception;

	/**
	 * 根据一定规则，从节点列表选出Node
	 * @param serverName 下游服务名
	 */
	Node chooseServer(String serverName);

	List<Node> getAllNodes (String serverName);

	String getRegion();
}
