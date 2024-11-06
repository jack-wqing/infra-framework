package com.jindi.infra.mq.kafka.constant;

import java.util.ArrayList;
import java.util.List;

import com.jindi.infra.mq.kafka.interceptor.CatConsumerInterceptor;
import com.jindi.infra.mq.kafka.interceptor.CatProducerInterceptor;

public class KafkaConsts {

	public static final List<String> PRODUCER_INTERCEPTORS = new ArrayList<>();
	public static final List<String> CONSUMER_INTERCEPTORS = new ArrayList<>();

	public static final String PROTOBUF_TYPE = "protobuf_type";

	static {
		PRODUCER_INTERCEPTORS.add(CatProducerInterceptor.class.getTypeName());
		CONSUMER_INTERCEPTORS.add(CatConsumerInterceptor.class.getTypeName());
	}
}
