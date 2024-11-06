package com.jindi.infra.grpc.pure;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.nacos.api.exception.NacosException;
import com.jindi.infra.grpc.pure.constant.CloudPlatformEnum;
import com.jindi.infra.grpc.pure.constant.EnvEnum;
import com.jindi.infra.grpc.pure.exception.AppNameNotFoundException;
import com.jindi.infra.grpc.pure.exception.CloudPlatformNotFoundException;
import com.jindi.infra.grpc.pure.exception.EnvNotFoundException;
import com.jindi.infra.grpc.pure.exception.ServiceNotFoundException;
import com.jindi.infra.grpc.pure.util.SimpleGrpcTools;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcClientPoolTest {

	@BeforeClass
	public static void setUp() throws ServiceNotFoundException, IOException, NacosException, AppNameNotFoundException,
			EnvNotFoundException, CloudPlatformNotFoundException {
		for (int port = 13000; port < 13003; port++) {
			GrpcServer.newBuilder().setAppName("greeter-server").setCloudPlatformEnums(CloudPlatformEnum.ALIYUN)
					.setEnv(EnvEnum.DEV).setBossNThreads(1).setWorkerNThreads(2).setPort(port)
					.enablePrometheusPush(true).addService(new GreeterImpl(port)).build();
		}
		for (int port = 13003; port < 13006; port++) {
			GrpcServer.newBuilder().setAppName("greeter-server").setCloudPlatformEnums(CloudPlatformEnum.HUAWEI)
					.setEnv(EnvEnum.DEV).setBossNThreads(1).setWorkerNThreads(2).setPort(port)
					.enablePrometheusPush(true).addService(new GreeterImpl(port)).build();
		}
	}

	@AfterClass
	public static void shutdown() throws Throwable {
	}

	@Test
	public void testCreate() throws Throwable {
		// 创建channel
		GrpcClientPool grpcClientPool = GrpcClientPool.newBuilder().setAppName("infra-grpc-pure")
				.setCloudPlatformEnums(CloudPlatformEnum.HUAWEI).setEnv(EnvEnum.DEV).enablePrometheusPush(true).enableMergeNacos(true).build();
		GreeterGrpc.GreeterBlockingStub greeterBlockingStub = SimpleGrpcTools.createBlockingStub(grpcClientPool,
				"greeter-server", GreeterGrpc.GreeterBlockingStub.class);
		for (int i = 0; i < 10000; i++) {
			try {
				HelloReply helloReply = greeterBlockingStub.withDeadlineAfter(100, TimeUnit.MILLISECONDS)
						.sayHello(HelloRequest.newBuilder().setName(RandomStringUtils.randomAlphabetic(32)).build());
				log.info("helloReply = {}", helloReply);
			} catch (Throwable e) {
				log.error("", e);
			}
		}
	}

	public static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

		private Integer port;

		public GreeterImpl(Integer port) {
			this.port = port;
		}

		@Override
		public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			HelloReply reply = HelloReply.newBuilder()
					.setMessage(String.format("Server port = %d; Hello %s", port, req.getName())).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}

}
