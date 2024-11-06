package com.jindi.infra.benchmark.client.proxy;

import com.jindi.infra.benchmark.UserDTO;
import com.jindi.infra.benchmark.UserService;
import com.jindi.infra.core.annotation.RPCFallback;

@RPCFallback
public class UserServiceFallback extends UserService {

	@Override
	public UserDTO getOrCreateUserDTO(UserDTO request) {
		return UserDTO.newBuilder().setId(1).setEmail("fallback@tianyancha.com").build();
	}
}
