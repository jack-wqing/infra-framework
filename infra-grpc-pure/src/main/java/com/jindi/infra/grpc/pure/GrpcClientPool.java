package com.jindi.infra.grpc.pure;

import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcClientInterceptor;
import com.alibaba.nacos.api.exception.NacosException;
import com.jindi.infra.grpc.Infra;
import com.jindi.infra.grpc.TycExtendGrpc;
import com.jindi.infra.grpc.pure.constant.CloudPlatformEnum;
import com.jindi.infra.grpc.pure.constant.EnvEnum;
import com.jindi.infra.grpc.pure.constant.MiddlewareConfig;
import com.jindi.infra.grpc.pure.loadbalance.NacosLoadBalancerProvider;
import com.jindi.infra.grpc.pure.discovery.NacosNameResolverProvider;
import com.jindi.infra.grpc.pure.exception.AppNameNotFoundException;
import com.jindi.infra.grpc.pure.exception.CloudPlatformNotFoundException;
import com.jindi.infra.grpc.pure.exception.EnvNotFoundException;
import com.jindi.infra.grpc.pure.metrics.CatClientInterceptor;
import com.jindi.infra.grpc.pure.metrics.CatInitializer;
import com.jindi.infra.grpc.pure.prometheus.PushGatewayInitializer;
import com.jindi.infra.grpc.pure.traffic.SentinelInitializer;
import io.grpc.*;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.InternalClientCalls;
import lombok.extern.slf4j.Slf4j;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringClientInterceptor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 缓存客户端，ping心跳包活
 */
@Slf4j
public class GrpcClientPool {

	private static final String RPC_CLIENT_PING_SCHEDULED_NAME = "rpc-client-ping-scheduled";
	private static final int INITIAL_DELAY_MILLISECONDS = 3000;
	private static final int PERIOD_MILLISECONDS = 3000;
	private static final int THREAD_POOL_SIZE = 4;
	private static final String PING_EXECUTOR_NAME = "ping-executor";
	private static final String INTERNAL_STUB_TYPE = "internal-stub-type";
	private static MonitoringClientInterceptor monitoringInterceptor = MonitoringClientInterceptor
			.create(Configuration.cheapMetricsOnly());
	private final ScheduledExecutorService scheduledExecutorService = Executors
			.newSingleThreadScheduledExecutor(r -> new Thread(r, RPC_CLIENT_PING_SCHEDULED_NAME));
	private final ExecutorService pingExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE,
			r -> new Thread(r, PING_EXECUTOR_NAME));
	private Map<String, ManagedChannel> managedChannelMap = new ConcurrentHashMap<>();
	private NameResolverProvider nameResolverProvider;
	private Boolean enableCat = true;
	private Boolean enableSentinel = true;
	private Boolean enablePrometheus = true;
	private Boolean enablePing = true;

	private GrpcClientPool(NameResolverProvider nameResolverProvider, Boolean enableCat, Boolean enableSentinel,
			Boolean enablePrometheus, Boolean enablePing) {
		this.nameResolverProvider = nameResolverProvider;
		this.enableCat = enableCat;
		this.enableSentinel = enableSentinel;
		this.enablePrometheus = enablePrometheus;
		this.enablePing = enablePing;
		pingStart();
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	private void pingStart() {
		if (!enablePing) {
			return;
		}
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			for (Map.Entry<String, ManagedChannel> entry : managedChannelMap.entrySet()) {
				String target = entry.getKey();
				ManagedChannel channel = entry.getValue();
				ping(target, channel);
			}
		}, INITIAL_DELAY_MILLISECONDS, PERIOD_MILLISECONDS, TimeUnit.MILLISECONDS);
	}

	private void ping(String target, ManagedChannel channel) {
		ClientCall<Infra.Empty, Infra.Empty> call = channel.newCall(TycExtendGrpc.getPingMethod(),
				CallOptions.DEFAULT.withExecutor(pingExecutor).withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
						.withOption(CallOptions.Key.create(INTERNAL_STUB_TYPE), InternalClientCalls.StubType.BLOCKING));
		try {
			ClientCalls.blockingUnaryCall(call, Infra.Empty.newBuilder().build());
			log.debug("心跳 {} 成功", target);
		} catch (Throwable e) {
			log.debug("ping", e);
		}
	}

	/**
	 * 创建客户端
	 *
	 * @param target
	 * @return
	 * @throws Throwable
	 */
	public ManagedChannel createManagedChannel(String target) {
		if (managedChannelMap.containsKey(target)) {
			return managedChannelMap.get(target);
		}
        ManagedChannelBuilder managedChannelBuilder = createManagedChannelBuilder(target);
        ManagedChannel managedChannel = managedChannelBuilder.build();
		managedChannelMap.put(target, managedChannel);
		return managedChannel;
	}

    public ManagedChannelBuilder createManagedChannelBuilder(String target) {
        NameResolverRegistry.getDefaultRegistry().register(nameResolverProvider);
        LoadBalancerRegistry.getDefaultRegistry().register(new NacosLoadBalancerProvider());
        ManagedChannelBuilder managedChannelBuilder = ManagedChannelBuilder.forTarget(target).usePlaintext()
                .defaultLoadBalancingPolicy("random_weight");
        initClientInterceptor(managedChannelBuilder);
        return managedChannelBuilder;
    }

    private void initClientInterceptor(ManagedChannelBuilder managedChannelBuilder) {
		if (enableCat) {
			managedChannelBuilder.intercept(new CatClientInterceptor());
		}
		if (enableSentinel) {
			managedChannelBuilder.intercept(new SentinelGrpcClientInterceptor());
		}
		if (enablePrometheus) {
			managedChannelBuilder.intercept(monitoringInterceptor);
		}
	}

	public static class Builder {

		private String appName;

		private CloudPlatformEnum cloudPlatform;

		private EnvEnum env;
		private Boolean enableCat = true;
		private Boolean enableSentinel = true;
		private Boolean enableSentinelInit = true;
		private Boolean enablePrometheus = true;
		private Boolean enablePrometheusPush = false;
		private Boolean enablePing = true;
		private Boolean enableMergeNacos = false;

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

		public Builder enableCat(Boolean enableCat) {
			this.enableCat = enableCat;
			return this;
		}

		public Builder enableSentinel(Boolean enableSentinel) {
			this.enableSentinel = enableSentinel;
			return this;
		}

        public Builder enableSentinelInit(Boolean enableSentinelInit) {
            this.enableSentinelInit = enableSentinelInit;
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

		public Builder enablePing(Boolean enablePing) {
			this.enablePing = enablePing;
			return this;
		}

		public Builder enableMergeNacos(Boolean enableMergeNacos) {
			this.enableMergeNacos = enableMergeNacos;
			return this;
		}

		public GrpcClientPool build()
				throws AppNameNotFoundException, EnvNotFoundException, NacosException, CloudPlatformNotFoundException {
			if (StringUtils.isBlank(appName)) {
				throw new AppNameNotFoundException();
			}
			if (cloudPlatform == null) {
				throw new CloudPlatformNotFoundException();
			}
			if (env == null) {
				throw new EnvNotFoundException();
			}
			MiddlewareConfig middlewareConfig = MiddlewareConfig.getMiddlewareConfigByCloudPlatformAndEnv(cloudPlatform,
					env);
			if (middlewareConfig == null) {
				throw new EnvNotFoundException();
			}
			if (enableCat) {
				CatInitializer.initCat(cloudPlatform, appName, env);
			}
			if (enableSentinel && enableSentinelInit) {
				SentinelInitializer.initSentinel(middlewareConfig, appName);
			}
			if (enablePrometheusPush) {
				PushGatewayInitializer.initPushGateway(appName, middlewareConfig);
			}
			List<String> nacosServerAddress = new ArrayList<>(2);
			if (enableMergeNacos) {
				MiddlewareConfig huaweiMiddlewareConfig = MiddlewareConfig
						.getMiddlewareConfigByCloudPlatformAndEnv(CloudPlatformEnum.HUAWEI, env);
				MiddlewareConfig aliyunMiddlewareConfig = MiddlewareConfig
						.getMiddlewareConfigByCloudPlatformAndEnv(CloudPlatformEnum.ALIYUN, env);
				nacosServerAddress.add(huaweiMiddlewareConfig.getNacosServerAddress());
				nacosServerAddress.add(aliyunMiddlewareConfig.getNacosServerAddress());
			} else {
				nacosServerAddress.add(middlewareConfig.getNacosServerAddress());
			}
			NameResolverProvider nameResolverProvider = NacosNameResolverProvider.newBuilder()
					.setServerAddress(nacosServerAddress)
					.setUsername(middlewareConfig.getNacosUsername()).setPassword(middlewareConfig.getNacosPassword())
					.build();
			return new GrpcClientPool(nameResolverProvider, enableCat, enableSentinel, enablePrometheus, enablePing);
		}
	}
}
