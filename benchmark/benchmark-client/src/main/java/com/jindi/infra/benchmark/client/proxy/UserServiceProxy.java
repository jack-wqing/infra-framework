package com.jindi.infra.benchmark.client.proxy;

import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ListenableFuture;
import com.jindi.infra.benchmark.UserDTO;
import com.jindi.infra.benchmark.UserService;
import com.jindi.infra.core.annotation.RPCCall;

@Service
public class UserServiceProxy {

	@RPCCall
	private UserService userService;

	public UserDTO getOrCreateUserDTO(UserDTO request) {
		return userService.getOrCreateUserDTO(request);
	}

	public ListenableFuture<UserDTO> getOrCreateUserDTOAsync(UserDTO request) {
		return userService.getOrCreateUserDTOAsync(request);
	}

}
