package com.jindi.infra.grpc.pure.loadbalance;

import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;

// nacos 负责均衡器实现
public class NacosLoadBalancerProvider extends LoadBalancerProvider {

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public int getPriority() {
		return 6;
	}

	@Override
	public String getPolicyName() {
		return "random_weight";
	}

	@Override
	public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
		return new NacosLoadBalancer(helper);
	}
}
