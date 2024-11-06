package com.jindi.infra.grpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jindi.infra.grpc.client.GreeterServiceFallback;
import com.jindi.infra.grpc.client.GreeterServiceProxy;
import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.extension.RequestFilter;
import com.jindi.infra.grpc.server.GreeterServiceImpl;
import com.jindi.infra.grpc.util.GrpcHeaderUtils;
import com.jindi.infra.grpc.util.NameUtils;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GreeterConfig {

	public static final ThreadLocal<String> CUSTOM_THREAD_LOCAL = new ThreadLocal<>();
	public static final String CUSTOM_KEY = "custom_key";

	@Bean
	public GreeterServiceImpl greeterServiceImpl() {
		return new GreeterServiceImpl();
	}

	@Bean
	public GreeterServiceProxy greeterServiceProxy() {
		return new GreeterServiceProxy();
	}

	@Bean
	public GreeterServiceFallback greeterServiceFallback() {
		return new GreeterServiceFallback();
	}

	@Bean
	public RequestFilter requestFilter() {
		return new RequestFilter() {

			private Cache<Long, Integer> random = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS)
					.maximumSize(10000).build();

			@Override
			public void before(Long id, MethodDescriptor method, Metadata headers) {
				int r = RandomUtils.nextInt();
				random.put(id, r);
				log.info("start method = {}, random = {}", NameUtils.getMethodName(RpcConsts.RPC_SERVER_TITLE, method),
						r);
				try {
					String value = GrpcHeaderUtils.getHeaderValue(CUSTOM_KEY, headers);
					log.info("header key: {}, value: {}", CUSTOM_KEY, value);
				} catch (Throwable e) {
					log.error("", e);
				}
			}

			@Override
			public void after(Long id, MethodDescriptor method, Throwable cause) {
				log.info("close method = {}, cause = {}, random = {}",
						NameUtils.getMethodName(RpcConsts.RPC_SERVER_TITLE, method), cause, random.getIfPresent(id));
				random.invalidate(id);
			}
		};
	}

	@Bean
	public CallInterceptor callInterceptor() {
		return new CallInterceptor() {

			private Cache<Long, Integer> random = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS)
					.maximumSize(10000).build();

			@Override
			public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
				int r = RandomUtils.nextInt();
				random.put(id, r);
				log.info("start method = {}, random = {}", NameUtils.getMethodName(RpcConsts.RPC_CLIENT_TITLE, method),
						r);
				try {
					String value = CUSTOM_THREAD_LOCAL.get();
					if (StringUtils.isNotBlank(value)) {
						extHeaders.put(CUSTOM_KEY, value);
					}
				} catch (Throwable e) {
					log.error("", e);
				}
			}

			@Override
			public void after(Long id, MethodDescriptor method, Throwable cause) {
				log.info("close method = {}, cause = {}, random = {}",
						NameUtils.getMethodName(RpcConsts.RPC_CLIENT_TITLE, method), cause, random.getIfPresent(id));
				random.invalidate(id);
			}
		};
	}
}
