package com.jindi.infra.core.constants;

public enum EventType {
	SERVICE_REGISTER_TSP(1, "服务注册TSP"), SERVICE_INSTANCE_LANE_TAGS_SYNC(2, "服务实例泳道tags同步"), RPC_CLIENT_FALLBACK(3,
			"rpc客户端回退"), RPC_CLIENT_MANUAL_DEGRADE(4, "rpc客户端手动降级"), RPC_CLIENT_SENTINEL_FLOW(5,
					"rpc客户端sentinel限流"), RPC_CLIENT_SENTINEL_DEGRADE(6, "rpc客户端sentinel降级");

	private final Integer no;
	private final String describe;

	EventType(Integer no, String describe) {
		this.no = no;
		this.describe = describe;
	}

	public Integer getNo() {
		return no;
	}

	public String getDescribe() {
		return describe;
	}
}
