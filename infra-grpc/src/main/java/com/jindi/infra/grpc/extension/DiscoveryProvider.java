package com.jindi.infra.grpc.extension;

import java.util.List;

public interface DiscoveryProvider {

	String ENV = "env";
	String SERVER_PORT = "serverPort";
	String SERVICES = "services";
	String TAGS_KEY = "tags";
	String REGION = "region";
	String REGISTRATION_TIME = "registrationTime";

	void register() throws Exception;


	void unregister() throws Exception;


	Node chooseServer(String serverName);

	List<Node> getAllNodes (String serverName);

	String getRegion();
}
