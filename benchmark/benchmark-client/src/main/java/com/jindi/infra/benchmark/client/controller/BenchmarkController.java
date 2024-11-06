package com.jindi.infra.benchmark.client.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jindi.infra.benchmark.client.feign.UserClient;
import com.jindi.infra.benchmark.client.proxy.UserServiceProxy;
import com.jindi.infra.benchmark.sdk.dto.UserDTO;
import com.jindi.infra.benchmark.sdk.service.UserService;
import com.jindi.infra.core.util.PbUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("benchmark")
@RestController
@Slf4j
public class BenchmarkController {

	@Autowired
	private UserClient userClient;
	@Autowired
	private UserServiceProxy userServiceProxy;
	/**
	 * TODO 这个配置需要随着服务器修改
	 */
	@Reference(check = false)
	private UserService userService;

	/**
	 * curl -X GET 'http://localhost:20000/benchmark/user'
	 *
	 * @return
	 */
	@GetMapping("user")
	public String grpc() throws Throwable {
		com.jindi.infra.benchmark.UserDTO.Builder userDTOBuilder = com.jindi.infra.benchmark.UserDTO.newBuilder()
				.setId(1L).setUsername("小展哥").setPassword("123456").setRealName("李茂展").setAddress("中国.北京市.海淀区.中关村")
				.setEmail("李茂展@gmail.com").setStatus(true).setRole(com.jindi.infra.benchmark.UserDTO.Role.COMMON)
				.setCreateTime(System.currentTimeMillis()).setUpdateTime(System.currentTimeMillis())
				.setTag("我是一根海草，随风摇摆摇摆");
		com.jindi.infra.benchmark.LabelDTO.Builder labelDTOBuilder = com.jindi.infra.benchmark.LabelDTO.newBuilder();
		labelDTOBuilder.setNo(1);
		labelDTOBuilder.setName("帅哥");
		userDTOBuilder.addLabels(labelDTOBuilder);
		labelDTOBuilder = com.jindi.infra.benchmark.LabelDTO.newBuilder();
		labelDTOBuilder.setNo(2);
		labelDTOBuilder.setName("暖男");
		userDTOBuilder.addLabels(labelDTOBuilder);
		com.jindi.infra.benchmark.UserDTO userDTO = userServiceProxy.getOrCreateUserDTO(userDTOBuilder.build());
		return PbUtils.toJSONString(userDTO);
	}

	/**
	 * curl -X GET 'http://localhost:20000/benchmark/user/feign'
	 *
	 * @return
	 */
	@GetMapping("user/feign")
	public String feign() {
		UserDTO.LabelDTO labelDTO1 = new UserDTO.LabelDTO();
		labelDTO1.setNo(1);
		labelDTO1.setName("帅哥");
		UserDTO.LabelDTO labelDTO2 = new UserDTO.LabelDTO();
		labelDTO2.setNo(2);
		labelDTO2.setName("暖男");
		UserDTO userDTO = UserDTO.builder().id(1L).username("小展哥").password("123456").realName("李茂展")
				.address("中国.北京市.海淀区.中关村").email("李茂展@gmail.com").status(true).role(UserDTO.Role.COMMON)
				.createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tag("我是一根海草，随风摇摆摇摆")
				.labelDTO(Arrays.asList(labelDTO1, labelDTO2)).build();
		userClient.getOrCreateUserDTO(userDTO);
		return "ok";
	}

	/**
	 * curl -X GET 'http://localhost:20000/benchmark/user/dubbo'
	 *
	 * @return
	 */
	@GetMapping("user/dubbo")
	public String dubbo() {
		UserDTO.LabelDTO labelDTO1 = new UserDTO.LabelDTO();
		labelDTO1.setNo(1);
		labelDTO1.setName("帅哥");
		UserDTO.LabelDTO labelDTO2 = new UserDTO.LabelDTO();
		labelDTO2.setNo(2);
		labelDTO2.setName("暖男");
		UserDTO userDTO = UserDTO.builder().id(1L).username("小展哥").password("123456").realName("李茂展")
				.address("中国.北京市.海淀区.中关村").email("李茂展@gmail.com").status(true).role(UserDTO.Role.COMMON)
				.createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tag("我是一根海草，随风摇摆摇摆")
				.labelDTO(Arrays.asList(labelDTO1, labelDTO2)).build();
		userService.getOrCreateUserDTO(userDTO);
		return "ok";
	}

	@GetMapping("checkAlive")
	public String checkAlive() {
		return "ok";
	}
}
