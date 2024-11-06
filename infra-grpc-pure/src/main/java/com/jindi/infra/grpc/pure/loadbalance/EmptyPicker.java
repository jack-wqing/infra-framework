package com.jindi.infra.grpc.pure.loadbalance;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import io.grpc.LoadBalancer;
import io.grpc.Status;

public final class EmptyPicker extends WeightRandomPicker {

	private final Status status;

	public EmptyPicker(@Nonnull Status status) {
		this.status = Preconditions.checkNotNull(status, "status");
	}

	@Override
	public LoadBalancer.PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
		return status.isOk() ? LoadBalancer.PickResult.withNoResult() : LoadBalancer.PickResult.withError(status);
	}

	@Override
	boolean isEquivalentTo(WeightRandomPicker picker) {
		return picker instanceof EmptyPicker && (Objects.equal(status, ((EmptyPicker) picker).status)
				|| (status.isOk() && ((EmptyPicker) picker).status.isOk()));
	}
}
