package com.jindi.infra.grpc.client;

import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ListenableFuture;
import com.jindi.infra.core.annotation.RPCCall;
import com.jindi.infra.grpc.GreeterService;
import com.jindi.infra.grpc.Hello;

@Service
public class GreeterServiceProxy {

	@RPCCall
	private GreeterService greeterService;

	public String hello(String name) {
		Hello.HelloReply helloReply = greeterService.sayHello(Hello.HelloRequest.newBuilder().setName(name).build());
		return helloReply.getMessage();
	}

	public ListenableFuture<Hello.HelloReply> helloAsync(String name) {
		return greeterService.sayHelloAsync(Hello.HelloRequest.newBuilder().setName(name).build());
	}
}
