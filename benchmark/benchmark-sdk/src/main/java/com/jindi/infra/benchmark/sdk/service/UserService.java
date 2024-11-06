package com.jindi.infra.benchmark.sdk.service;

import com.jindi.infra.benchmark.sdk.dto.UserDTO;

public interface UserService {

	UserDTO getOrCreateUserDTO(UserDTO userDTO);
}
