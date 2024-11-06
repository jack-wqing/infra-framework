package com.jindi.infra.governance;

import com.external.jindi.infra.OpenFeignGovernanceAutoConfiguration;
import com.jindi.infra.governance.event.TspInfraEventPublisher;
import com.jindi.infra.governance.lane.LaneFilter;
import com.jindi.infra.governance.lane.LaneOpenFeignRequestInterceptor;
import com.jindi.infra.governance.lane.grpc.LaneCallInterceptor;
import com.jindi.infra.governance.lane.grpc.LaneGrpcCoreServerInterceptor;
import com.jindi.infra.governance.manage.AsyncServiceManager;
import com.jindi.infra.registry.RegistryAutoConfiguration;
import feign.Feign;
import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author changbo
 * @date 2021/8/29
 */
@Slf4j
@Configuration
@Import({GovernanceAutoConfiguration.RpcGovernanceAutoConfiguration.class, RegistryAutoConfiguration.class,
		GovernanceAutoConfiguration.OpenFeignGovernanceAutoConfiguration.class})
@ConditionalOnProperty(name = "rpc.register", havingValue = "true", matchIfMissing = true)
@RibbonClients(defaultConfiguration = OpenFeignGovernanceAutoConfiguration.class)
public class GovernanceAutoConfiguration {


	@ConditionalOnWebApplication
	@ConditionalOnMissingBean(name = "laneFilter")
	@Bean(name = "laneFilter")
	public FilterRegistrationBean<LaneFilter> laneFilter() {
		FilterRegistrationBean<LaneFilter> registration = new FilterRegistrationBean<>();
		LaneFilter filter = new LaneFilter();
		registration.setFilter(filter);
		registration.addUrlPatterns("/*");
		registration.setName("lane-filter");
		return registration;
	}

	@Configuration
	@ConditionalOnProperty(name = "rpc.enable", havingValue = "true", matchIfMissing = true)
	@ConditionalOnClass({ServerInterceptor.class, ClientInterceptor.class})
	public static class RpcGovernanceAutoConfiguration {

		@ConditionalOnMissingBean(name = "asyncServiceManager")
		@Bean(name = "asyncServiceManager")
		public AsyncServiceManager asyncServiceManager() {
			return new AsyncServiceManager();
		}

		@ConditionalOnMissingBean(name = "tspInfraEventPublisher")
		@Bean(name = "tspInfraEventPublisher")
		public TspInfraEventPublisher tspInfraEventPublisher() {
			return new TspInfraEventPublisher();
		}

		@ConditionalOnMissingBean(name = "laneCallInterceptor")
		@Bean(name = "laneCallInterceptor")
		public LaneCallInterceptor laneCallInterceptor() {
			return new LaneCallInterceptor();
		}

		@ConditionalOnMissingBean(name = "laneGrpcCoreServerInterceptor")
		@Bean(name = "laneGrpcCoreServerInterceptor")
		public LaneGrpcCoreServerInterceptor laneGrpcCoreServerInterceptor() {
			return new LaneGrpcCoreServerInterceptor();
		}
	}

	@Configuration
	@ConditionalOnClass({Feign.class})
	public class OpenFeignGovernanceAutoConfiguration {

		@ConditionalOnMissingBean(name = "laneOpenFeignRequestInterceptor")
		@Bean(name = "laneOpenFeignRequestInterceptor")
		public LaneOpenFeignRequestInterceptor laneOpenFeignRequestInterceptor() {
			return new LaneOpenFeignRequestInterceptor();
		}
	}

}
