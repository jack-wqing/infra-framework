package com.jindi.infra.grpc.server;

import com.jindi.infra.grpc.GreeterConfig;
import com.jindi.infra.core.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import com.jindi.infra.core.annotation.RPCService;
import com.jindi.infra.grpc.BusinessException;
import com.jindi.infra.grpc.GreeterService;
import com.jindi.infra.grpc.Hello;

@Slf4j
@RPCService
public class GreeterServiceImpl extends GreeterService {

	@Override
	public Hello.HelloReply sayHello(Hello.HelloRequest request) {
		try {
			Thread.sleep(50);
		} catch (Throwable e) {
		}
		String value = ContextUtils.getContextValue(GreeterConfig.CUSTOM_KEY);
		log.info("value = {}", value);
		if (RandomUtils.nextBoolean()) {
			throw new BusinessException("业务异常", new IllegalArgumentException("用户必须登陆才能访问"));
		}
		return Hello.HelloReply.newBuilder().setMessage(String.format("hi %s", request.getName())).build();
	}
}
