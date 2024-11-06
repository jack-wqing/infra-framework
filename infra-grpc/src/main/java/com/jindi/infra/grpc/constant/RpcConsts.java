package com.jindi.infra.grpc.constant;

public class RpcConsts {

	public static final String RPC_CLIENT_TITLE = "Rpc.Client";

	public static final String RPC_SERVER_TITLE = "Rpc.Server";

	public static final String GATEWAY_ROUTING_KEY = "application";
	public static final ThreadLocal<String> GATEWAY_ROUTING_VALUE = new ThreadLocal<>();
	public static String SERVICE_INFO;
}
