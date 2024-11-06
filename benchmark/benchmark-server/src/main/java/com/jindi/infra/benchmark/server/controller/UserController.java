package com.jindi.infra.benchmark.server.controller;

import com.jindi.infra.benchmark.server.remote.RemoteTopologyService;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jindi.infra.benchmark.sdk.dto.UserDTO;
import com.jindi.infra.benchmark.sdk.service.UserService;
import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController implements UserService {

	@Resource
	private RemoteTopologyService remoteTopologyService;

	@PostMapping
	@Override
	public UserDTO getOrCreateUserDTO(@RequestBody UserDTO userDTO) {
		log.info("userDTO = {}", InnerJSONUtils.toJSONString(userDTO));
		log.info("topology value: {}", TopologyHeaderUtil.getPreviousChain());
		remoteTopologyService.getCallTypes();
		return userDTO;
	}
}
