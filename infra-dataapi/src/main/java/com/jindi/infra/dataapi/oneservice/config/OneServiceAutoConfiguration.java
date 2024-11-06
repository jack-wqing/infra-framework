package com.jindi.infra.dataapi.oneservice.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jindi.infra.dataapi.oneservice.aspect.OneServiceAspect;
import com.jindi.infra.dataapi.oneservice.builder.OneServicePrefixDiscoveryBuilder;
import com.jindi.infra.dataapi.oneservice.builder.OneServicePrefixDomainBuilder;
import com.jindi.infra.dataapi.oneservice.call.RestTemplateCall;
import com.jindi.infra.dataapi.oneservice.client.OneServiceHttpClient;
import com.jindi.infra.dataapi.oneservice.controller.OneServiceConnectController;
import com.jindi.infra.dataapi.oneservice.discovery.OneServiceDiscoveryService;
import com.jindi.infra.dataapi.oneservice.locator.OneServiceDiscoveryLocator;
import com.jindi.infra.dataapi.oneservice.locator.OneServiceUrlLocator;
import com.jindi.infra.dataapi.oneservice.properties.OneServiceConfigProperties;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.httpcomponents.PoolingHttpClientConnectionManagerMetricsBinder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties({OneServiceConfigProperties.class})
@RestController
public class OneServiceAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public OneServiceAspect oneServiceAspect() {
		return new OneServiceAspect();
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServiceHttpClient oneServiceHttpClient() {
		return new OneServiceHttpClient();
	}

//	@ConditionalOnMissingBean
//	@Bean
//	public RestTemplateCall restTemplateCall(OneServiceConfigProperties oneServiceConfigProperties) {
//		ConnectionPool pool = new ConnectionPool(oneServiceConfigProperties.getMaxIdleConnections(), oneServiceConfigProperties.getKeepAlive(), TimeUnit.MILLISECONDS);
//		OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectionPool(pool)
//				.retryOnConnectionFailure(true)
//				.connectTimeout(oneServiceConfigProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
//				.readTimeout(oneServiceConfigProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
//				.writeTimeout(oneServiceConfigProperties.getWriteTimeout(), TimeUnit.MILLISECONDS)
//				.build();
//		OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
//		RestTemplate restTemplate = new RestTemplate(factory);
//		return new RestTemplateCall(restTemplate);
//	}

//	@Bean
//	public HttpComponentsConnectionPoolMetrics httpComponentsConnectionPoolMetrics() {
//		return new HttpComponentsConnectionPoolMetrics();
//	}

	@Bean
	public OneServiceConnectController oneServiceConnectController() {
		return new OneServiceConnectController();
	}

	@Bean
	public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager(MeterRegistry meterRegistry, OneServiceConfigProperties properties) {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(properties.getMaxConnections()); // 设置最大连接数
		connectionManager.setDefaultMaxPerRoute(properties.getMaxPerRouteConnections()); // 设置每个路由的最大连接数
		connectionManager.setValidateAfterInactivity(5000); // 设置连接的验证时间
		new PoolingHttpClientConnectionManagerMetricsBinder(connectionManager, "HttpComponentsConnPool")
				.bindTo(meterRegistry);
		return connectionManager;
	}

	@ConditionalOnMissingBean
	@Bean
	public RestTemplateCall restTemplateCall(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, OneServiceConfigProperties properties) {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(properties.getConnectionRequestTimeout()) // 从连接池获取连接的超时时间
				.setConnectTimeout(properties.getConnectTimeout()) // 连接建立的超时时间
				.setSocketTimeout(properties.getSocketTimeout()) // 数据传输的超时时间
				.build();

		// 创建连接池
		CloseableHttpClient httpClient = HttpClients.custom()
				.setMaxConnTotal(properties.getMaxConnections()) // 设置最大连接数
				.setMaxConnPerRoute(properties.getMaxPerRouteConnections()) // 设置每个路由的最大连接数
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(poolingHttpClientConnectionManager)
				.evictIdleConnections(properties.getIdleConnectTime(), java.util.concurrent.TimeUnit.MILLISECONDS) // 设置空闲连接的超时时间
				.build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(factory);
		return new RestTemplateCall(restTemplate);
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServiceUrlLocator oneServiceUrlLocator(OneServiceConfigProperties oneServiceConfigProperties) {
		return new OneServiceUrlLocator(oneServiceConfigProperties);
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServiceDiscoveryLocator oneServiceDiscoveryLocator(OneServiceConfigProperties oneServiceConfigProperties) {
		return new OneServiceDiscoveryLocator(oneServiceConfigProperties);
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServicePrefixDomainBuilder oneServicePrefixDomainBuilder() {
		return new OneServicePrefixDomainBuilder();
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServicePrefixDiscoveryBuilder oneServicePrefixDiscoveryBuilder() {
		return new OneServicePrefixDiscoveryBuilder();
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServiceConfigPropertiesRefresher oneServiceConfigPropertiesRefresher() {
		return new OneServiceConfigPropertiesRefresher();
	}

	@ConditionalOnMissingBean
	@Bean
	public OneServiceDiscoveryService oneServiceDiscoveryService() {
		return new OneServiceDiscoveryService();
	}
}
