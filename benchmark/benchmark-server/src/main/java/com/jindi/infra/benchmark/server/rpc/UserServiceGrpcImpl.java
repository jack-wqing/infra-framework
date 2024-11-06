package com.jindi.infra.benchmark.server.rpc;

import com.jindi.infra.benchmark.UserDTO;
import com.jindi.infra.benchmark.UserService;
import com.jindi.infra.benchmark.server.remote.RemoteTopologyService;
import com.jindi.infra.core.annotation.RPCService;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;


@Slf4j
@RPCService
public class UserServiceGrpcImpl extends UserService {

	@Resource
	private RemoteTopologyService remoteTopologyService;

	@Override
	public UserDTO getOrCreateUserDTO(UserDTO request) {
		remoteTopologyService.getCallTypes();
		log.info("topology value: {}", TopologyHeaderUtil.getPreviousChain());
		return request;
	}
}
