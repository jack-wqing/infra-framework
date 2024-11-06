package com.jindi.infra.metrics.prometheus.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.metrics.prometheus.handler.RpcInvokeEventListener;
import com.jindi.infra.metrics.prometheus.metrics.ContainerProcessCpuLoadMetrics;
import com.jindi.infra.metrics.prometheus.metrics.GrpcClientMetric;
import com.jindi.infra.metrics.prometheus.metrics.GrpcServerMetric;
import com.jindi.infra.metrics.prometheus.metrics.HttpServerResponsePercentilesMetrics;

import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.CollectorRegistry;
import me.dinowernli.grpc.prometheus.MonitoringClientInterceptor;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;

@Configuration
@AutoConfigureAfter(PrometheusMetricsExportAutoConfiguration.class)
public class PrometheusAutoConfiguration {

	@Configuration
	@ConditionalOnProperty(name = "rpc.enable", havingValue = "true", matchIfMissing = true)
	@ConditionalOnClass({ClientInterceptor.class, ServerInterceptor.class})
	public static class GrpcPrometheusAutoConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public GrpcClientMetric grpcClientMetric() {
			return new GrpcClientMetric();
		}

		@ConditionalOnMissingBean
		@Bean
		public GrpcServerMetric grpcServerMetric() {
			return new GrpcServerMetric();
		}

		@ConditionalOnBean(CollectorRegistry.class)
		@ConditionalOnMissingBean
		@Bean
		public MonitoringClientInterceptor monitoringClientInterceptor(GrpcClientMetric grpcClientMetric, CollectorRegistry collectorRegistry) {
			return grpcClientMetric.createGrpcClientInterceptor(collectorRegistry);
		}

		@ConditionalOnBean(CollectorRegistry.class)
		@ConditionalOnMissingBean
		@Bean
		public MonitoringServerInterceptor monitoringServerInterceptor(GrpcServerMetric grpcServerMetric, CollectorRegistry collectorRegistry) {
			return grpcServerMetric.createGrpcServerInterceptor(collectorRegistry);
		}

		@ConditionalOnBean(CollectorRegistry.class)
		@ConditionalOnMissingBean
		@Bean
		public RpcInvokeEventListener rpcInvokeEventListener(CollectorRegistry collectorRegistry) {
			return new RpcInvokeEventListener(collectorRegistry);
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "http.percentile.enable", havingValue = "true", matchIfMissing = true)
	public static class HttpPrometheusAutoConfiguration {

		@ConditionalOnMissingBean
		@Bean
		MeterRegistryCustomizer<MeterRegistry> httpServerResponsePercentilesMetrics() {
			return new HttpServerResponsePercentilesMetrics();
		}
	}

	@ConditionalOnMissingBean
	@Bean
	public ContainerProcessCpuLoadMetrics containerProcessCpuLoadMetrics() {
		return new ContainerProcessCpuLoadMetrics();
	}

}
