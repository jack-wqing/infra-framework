package com.jindi.infra.benchmark.client.feign;

import org.springframework.cloud.openfeign.FeignClient;

import com.jindi.infra.benchmark.sdk.dto.UserDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "benchmark-server")
@RequestMapping("user")
public interface UserClient {

	@PostMapping
	UserDTO getOrCreateUserDTO(@RequestBody UserDTO userDTO);
}
