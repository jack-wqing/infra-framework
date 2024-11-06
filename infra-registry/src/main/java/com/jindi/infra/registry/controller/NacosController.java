package com.jindi.infra.registry.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.jindi.infra.registry.model.Result;
import com.jindi.infra.registry.nacos.provider.HuaweiNacosDiscoveryProvider;

@RestController
public class NacosController {

	@Autowired(required = false)
	private HuaweiNacosDiscoveryProvider discoveryProvider;

	/**
	 * 摘掉实例
	 *
	 * @param serviceName
	 * @param ip
	 * @param port
	 * @return
	 */
	@GetMapping("/nacos/disable")
	public Result disable(@RequestParam(value = "serviceName") String serviceName,
			@RequestParam(value = "ip") String ip, @RequestParam(value = "port") Integer port) {
		Result resp = new Result();
		if (StringUtils.isAnyBlank(serviceName, ip) || port == null || port <= 0) {
			resp.setSuccess(false);
			resp.setMessage("parameter error");
			return resp;
		}
		if (discoveryProvider == null) {
			resp.setSuccess(false);
			resp.setMessage("discoveryProvider is not initialized. The service may use direct connection mode");
			return resp;
		}
		if (discoveryProvider.disableInstance(serviceName, ip, port)) {
			resp.setSuccess(true);
			resp.setMessage("ok");
			return resp;
		}
		resp.setSuccess(false);
		resp.setMessage(String.format(
				"The service %s:%d is not in the routing table. Please call the /nacos/register interface first", ip,
				port));
		return resp;
	}

	/**
	 * 添加实例
	 *
	 * @param serviceName
	 * @param ip
	 * @param port
	 * @return
	 */
	@GetMapping("/nacos/enable")
	public Result enable(@RequestParam(value = "serviceName") String serviceName, @RequestParam(value = "ip") String ip,
			@RequestParam(value = "port") Integer port) {
		Result result = new Result();
		if (StringUtils.isAnyBlank(serviceName, ip) || port == null || port <= 0) {
			result.setSuccess(false);
			result.setMessage("parameter error");
			return result;
		}
		if (discoveryProvider == null) {
			result.setSuccess(false);
			result.setMessage("discoveryProvider is not initialized. The service may use direct connection mode");
			return result;
		}
		if (discoveryProvider.enableInstance(serviceName, ip, port)) {
			result.setSuccess(true);
			result.setMessage("ok");
			return result;
		}
		result.setSuccess(false);
		result.setMessage(String.format(
				"The service %s:%d is not in the routing table. Please call the /nacos/register interface first", ip,
				port));
		return result;
	}

	/**
	 * 获取服务的所有实例
	 *
	 * @param serviceName
	 * @return
	 */
	@GetMapping("/nacos/instances")
	public Result getInstances(@RequestParam(value = "serviceName") String serviceName) {
		Result result = new Result();
		if (StringUtils.isBlank(serviceName)) {
			result.setSuccess(false);
			result.setMessage("parameter error");
			return result;
		}
		if (discoveryProvider == null) {
			result.setSuccess(false);
			result.setMessage("discoveryProvider is not initialized. The service may use direct connection mode");
			return result;
		}
		List<Instance> instances = discoveryProvider.getAllInstances(serviceName);
		if (CollectionUtils.isEmpty(instances)) {
			result.setSuccess(false);
			result.setMessage(String.format("Service %s is not registered", serviceName));
			return result;
		}
		result.setSuccess(true);
		result.setMessage("ok");
		result.setData(instances);
		return result;
	}
}
