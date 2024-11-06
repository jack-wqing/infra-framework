package com.jindi.infra.grpc;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.protobuf.GeneratedMessageV3;
import com.jindi.infra.core.util.PbUtils;
import com.jindi.infra.grpc.client.GreeterServiceProxy;
import com.jindi.infra.grpc.config.GrpcAutoConfiguration;
import com.jindi.infra.grpc.util.FutureGroup;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {GrpcAutoConfiguration.class, GreeterConfig.class})
@Slf4j
public class GreeterServiceProxyTests {

	@Autowired
	private GreeterServiceProxy greeterServiceProxy;

	@BeforeAll
	public static void setUp() {
		log.info("RPC测试用例开始");
	}

	@AfterAll
	public static void afterAll() {
		log.info("完成RPC测试用例");
	}

	@Test
	public void testHello() {
		long startTime = System.currentTimeMillis();
		for (int no = 0; no < 100; no++) {
			GreeterConfig.CUSTOM_THREAD_LOCAL.set(String.valueOf(no));
			try {
				log.info(greeterServiceProxy.hello(String.format("name:%d", no)));
			} catch (Throwable e) {
				log.error("异常:", e);
			}
		}
		log.info("同步调用耗时: {} 毫秒", System.currentTimeMillis() - startTime);
	}

	@Test
	public void testHelloAsync() throws InterruptedException {
		int size = 50;
		// 创建Future组
		FutureGroup futureGroup = FutureGroup.create();
		for (int no = 0; no < size; no++) {
			int finalNo = no;
			// 提交Future任务
			futureGroup.submit(() -> greeterServiceProxy.helloAsync(String.format("name:%d", finalNo)));
			GreeterConfig.CUSTOM_THREAD_LOCAL.set(String.valueOf(no));
		}
		long startTime = System.currentTimeMillis();
		// 等待批量任务结束, 获取结果
		List<GeneratedMessageV3> values = futureGroup.blockingGet(300);
		for (GeneratedMessageV3 messageV3 : values) {
			log.info(PbUtils.toJSONString(messageV3));
		}
		log.info("异步调用耗时: {} 毫秒", System.currentTimeMillis() - startTime);
	}
}
