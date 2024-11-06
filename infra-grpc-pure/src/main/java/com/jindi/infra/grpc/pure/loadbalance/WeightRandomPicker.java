package com.jindi.infra.grpc.pure.loadbalance;

import io.grpc.LoadBalancer;

public abstract class WeightRandomPicker extends LoadBalancer.SubchannelPicker {

	/**
	 * @param picker
	 * @return
	 */
	abstract boolean isEquivalentTo(WeightRandomPicker picker);
}
