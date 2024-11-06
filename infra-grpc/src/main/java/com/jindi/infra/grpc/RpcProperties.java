package com.jindi.infra.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.jindi.infra.grpc.client.ClientProperties;
import com.jindi.infra.grpc.server.ServerProperties;

import lombok.Data;

@Data
@ConfigurationProperties("rpc")
public class RpcProperties {

	private Boolean enable = true;
	private ServerProperties server = new ServerProperties();
	private ClientProperties client = new ClientProperties();
	// RPC maxsize 11M
	private Integer maxPayLoad = 1024 * 1024 * 11;

	private SecurityProperties security = new SecurityProperties();
}
