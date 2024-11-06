package com.jindi.infra.grpc.pure.loadbalance;

public class Ref<T> {
	T value;

	public Ref(T value) {
		this.value = value;
	}
}
