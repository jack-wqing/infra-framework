package com.jindi.infra.grpc.pure.constant;

import io.grpc.Attributes;

public class DiscoveryConsts {

	public static final String REGISTRATION_TIME_KEY = "registrationTime";
	public static final Attributes.Key WEIGHT_ATTRIBUTES_KEY = Attributes.Key.create("weight");
	public static final Attributes.Key REGISTRATION_TIME_ATTRIBUTES_KEY = Attributes.Key.create(REGISTRATION_TIME_KEY);

}
