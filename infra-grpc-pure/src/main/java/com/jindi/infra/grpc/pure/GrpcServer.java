package com.jindi.infra.grpc.pure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcServerInterceptor;
import com.alibaba.nacos.api.exception.NacosException;
import com.jindi.infra.grpc.Infra;
import com.jindi.infra.grpc.TycExtendGrpc;
import com.jindi.infra.grpc.pure.constant.CloudPlatformEnum;
import com.jindi.infra.grpc.pure.constant.EnvEnum;
import com.jindi.infra.grpc.pure.constant.MiddlewareConfig;
import com.jindi.infra.grpc.pure.discovery.ServerRegister;
import com.jindi.infra.grpc.pure.exception.AppNameNotFoundException;
import com.jindi.infra.grpc.pure.exception.CloudPlatformNotFoundException;
import com.jindi.infra.grpc.pure.exception.EnvNotFoundException;
import com.jindi.infra.grpc.pure.exception.ServiceNotFoundException;
import com.jindi.infra.grpc.pure.metrics.CatInitializer;
import com.jindi.infra.grpc.pure.metrics.CatServerInterceptor;
import com.jindi.infra.grpc.pure.prometheus.PushGatewayInitializer;
import com.jindi.infra.grpc.pure.traffic.SentinelInitializer;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;

@Slf4j
public class GrpcServer {

	private static final int WORKER_N_THREADS = 200;
	private static final int BOSS_N_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	private ServerRegister serverRegister;
	private Server server;

	private GrpcServer(ServerRegister serverRegister, Server server) {
		this.serverRegister = serverRegister;
		this.server = server;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public void awaitTermination(Integer seconds) throws Throwable {
		if (server != null) {
			serverRegister.stop();
			server.awaitTermination(seconds, TimeUnit.SECONDS);
		}
	}

	public static class Builder {

		private static MonitoringServerInterceptor monitoringInterceptor = MonitoringServerInterceptor
				.create(Configuration.cheapMetricsOnly());
		private final List<BindableService> services = new ArrayList<>();
		private String appName;
		private CloudPlatformEnum cloudPlatform;
		private EnvEnum env;
		private Integer port = 9999;
		private Integer bossNThreads = BOSS_N_THREADS;
		private Integer workerNThreads = WORKER_N_THREADS;
		private Boolean enableCat = true;
		private Boolean enableSentinel = true;
		private Boolean enablePrometheus = true;
		private Boolean enablePrometheusPush = false;

		public Builder setAppName(String appName) {
			this.appName = appName;
			return this;
		}

		public Builder setCloudPlatformEnums(CloudPlatformEnum cloudPlatform) {
			this.cloudPlatform = cloudPlatform;
			return this;
		}

		public Builder setEnv(EnvEnum env) {
			this.env = env;
			return this;
		}

		public Builder addService(BindableService... service) {
			services.addAll(Arrays.asList(service));
			return this;
		}

		public Builder setPort(Integer port) {
			this.port = port;
			return this;
		}

		public Builder setBossNThreads(Integer bossNThreads) {
			this.bossNThreads = bossNThreads;
			return this;
		}

		public Builder setWorkerNThreads(Integer workerNThreads) {
			this.workerNThreads = workerNThreads;
			return this;
		}

		public Builder enableCat(Boolean enableCat) {
			this.enableCat = enableCat;
			return this;
		}

		public Builder enableSentinel(Boolean enableSentinel) {
			this.enableSentinel = enableSentinel;
			return this;
		}

		public Builder enablePrometheus(Boolean enablePrometheus) {
			this.enablePrometheus = enablePrometheus;
			return this;
		}

		public Builder enablePrometheusPush(Boolean enablePrometheusPush) {
			this.enablePrometheusPush = enablePrometheusPush;
			return this;
		}

		public GrpcServer build() throws IOException, ServiceNotFoundException, AppNameNotFoundException,
				EnvNotFoundException, NacosException, CloudPlatformNotFoundException {
			if (StringUtils.isBlank(appName)) {
				throw new AppNameNotFoundException();
			}
			if (cloudPlatform == null) {
				throw new CloudPlatformNotFoundException();
			}
			if (env == null) {
				throw new EnvNotFoundException();
			}
			if (services.isEmpty()) {
				throw new ServiceNotFoundException();
			}
			MiddlewareConfig middlewareConfig = MiddlewareConfig.getMiddlewareConfigByCloudPlatformAndEnv(cloudPlatform,
					env);
			if (middlewareConfig == null) {
				throw new EnvNotFoundException();
			}
			if (enableCat) {
				CatInitializer.initCat(cloudPlatform, appName, env);
			}
			if (enableSentinel) {
				SentinelInitializer.initSentinel(middlewareConfig, appName);
			}
			NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port)
					.channelType(NioServerSocketChannel.class).bossEventLoopGroup(new NioEventLoopGroup(bossNThreads))
					.workerEventLoopGroup(new NioEventLoopGroup(workerNThreads));
			for (BindableService service : services) {
				serverBuilder.addService(service);
			}
			serverBuilder.addService(new TycExtendGrpc.TycExtendImplBase() {
				@Override
				public void ping(Infra.Empty request, StreamObserver<Infra.Empty> responseObserver) {
					responseObserver.onNext(request);
					responseObserver.onCompleted();
				}
			});
			if (enableCat) {
				serverBuilder.intercept(new CatServerInterceptor());
			}
			if (enableSentinel) {
				serverBuilder.intercept(new SentinelGrpcServerInterceptor());
			}
			if (enablePrometheus) {
				serverBuilder.intercept(monitoringInterceptor);
			}
			if (enablePrometheusPush) {
				PushGatewayInitializer.initPushGateway(appName, middlewareConfig);
			}
			Server server = serverBuilder.build().start();
			ServerRegister serverRegister = ServerRegister.newBuilder().setAppName(appName)
					.setServerAddress(middlewareConfig.getNacosServerAddress())
					.setUsername(middlewareConfig.getNacosUsername()).setPassword(middlewareConfig.getNacosPassword())
					.build();
			serverRegister.setPort(port);
			serverRegister.start();
			return new GrpcServer(serverRegister, server);
		}
	}
}
