package com.jindi.infra.grpc.client;

import com.jindi.infra.core.annotation.RPCCallOption;
import com.jindi.infra.core.annotation.RPCFallback;
import com.jindi.infra.grpc.BusinessException;
import com.jindi.infra.grpc.GreeterService;
import com.jindi.infra.grpc.Hello;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RPCFallback
public class GreeterServiceFallback extends GreeterService {

	@RPCCallOption(exception = BusinessException.class)
	@Override
	public Hello.HelloReply sayHello(Hello.HelloRequest request) {
		return Hello.HelloReply.newBuilder()
				.setMessage(String.format("greeter hello name = %s fallback", request.getName())).build();
	}
}
