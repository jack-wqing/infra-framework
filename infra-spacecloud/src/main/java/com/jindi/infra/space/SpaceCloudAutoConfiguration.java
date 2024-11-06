package com.jindi.infra.space;

import com.jindi.infra.space.client.GraphQLHttpClients;
import com.jindi.infra.space.client.RomaHttpClient;
import com.jindi.infra.space.client.SpaceCloudHttpClient;
import com.jindi.infra.space.config.EndpointAspect;
import com.jindi.infra.space.config.SpaceCloudConfigPropertiesRefresher;
import com.jindi.infra.space.properties.RomaConfigProperties;
import com.jindi.infra.space.properties.SpaceCloudConfigProperties;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties({SpaceCloudConfigProperties.class, RomaConfigProperties.class})
public class SpaceCloudAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public GraphQLHttpClients graphQLHttpClients () {
		return new GraphQLHttpClients();
	}

	@ConditionalOnMissingBean
	@Bean
	public EndpointAspect endpointAspect() {
		return new EndpointAspect();
	}

	@ConditionalOnMissingBean
	@Bean
	public SpaceCloudHttpClient spaceCloudHttpClient(SpaceCloudConfigProperties spaceCloudConfigProperties) {
		ConnectionPool pool = new ConnectionPool(spaceCloudConfigProperties.getMaxIdleConnections(), spaceCloudConfigProperties.getKeepAlive(), TimeUnit.MILLISECONDS);
		OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectionPool(pool)
				.retryOnConnectionFailure(true)
				.connectTimeout(spaceCloudConfigProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
				.readTimeout(spaceCloudConfigProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
				.writeTimeout(spaceCloudConfigProperties.getWriteTimeout(), TimeUnit.MILLISECONDS)
				.build();
		OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
		RestTemplate restTemplate = new RestTemplate(factory);
		return new SpaceCloudHttpClient(restTemplate);
	}

	@ConditionalOnMissingBean
	@Bean
	public RomaHttpClient romaHttpClient(RomaConfigProperties romaConfigProperties) {
		ConnectionPool pool = new ConnectionPool(romaConfigProperties.getMaxIdleConnections(), romaConfigProperties.getKeepAlive(), TimeUnit.MILLISECONDS);
		OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectionPool(pool)
				.retryOnConnectionFailure(true)
				.connectTimeout(romaConfigProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
				.readTimeout(romaConfigProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
				.writeTimeout(romaConfigProperties.getWriteTimeout(), TimeUnit.MILLISECONDS)
				.build();
		OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
		RestTemplate restTemplate = new RestTemplate(factory);
		return new RomaHttpClient(restTemplate);
	}

	@ConditionalOnMissingBean
	@Bean
	public SpaceCloudUrlLocator spaceCloudUrlLocator(SpaceCloudConfigProperties spaceCloudConfigProperties) {
		return new SpaceCloudUrlLocator(spaceCloudConfigProperties);
	}

	@ConditionalOnMissingBean
	@Bean
	public SpaceCloudConfigPropertiesRefresher spaceCloudConfigPropertiesRefresher() {
		return new SpaceCloudConfigPropertiesRefresher();
	}
}
