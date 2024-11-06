package com.jindi.infra.benchmark.server.rpc;

import com.alibaba.dubbo.config.annotation.Service;
import com.jindi.infra.benchmark.sdk.dto.UserDTO;
import com.jindi.infra.benchmark.sdk.service.UserService;
import com.jindi.infra.benchmark.server.remote.RemoteTopologyService;
import com.jindi.infra.core.util.ContextUtils;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@org.springframework.stereotype.Service
@Slf4j
@Service
public class UserServiceDubboImpl implements UserService {

//	@Resource
//	private RemoteTopologyService remoteTopologyService;

	@Override
	public UserDTO getOrCreateUserDTO(UserDTO userDTO) {
		log.info("topology value: {}", TopologyHeaderUtil.getPreviousChain());
		return userDTO;
	}
}
