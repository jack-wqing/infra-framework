package com.jindi.infra.grpc.pure.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

public class NacosNameResolverProvider extends NameResolverProvider {

	private static final String NACOS_SCHEME = "nacos";
	private final List<NamingService> namingServices;

	private NacosNameResolverProvider(List<NamingService> namingServices) {
		this.namingServices = namingServices;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	@Override
	protected boolean isAvailable() {
		return true;
	}

	@Override
	protected int priority() {
		return 5;
	}

	@Override
	public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
		return new NacosNameResolver(namingServices, targetUri);
	}

	@Override
	public String getDefaultScheme() {
		return NACOS_SCHEME;
	}

	public static class Builder {

		private List<String> serverAddresses = Collections.singletonList("localhost:8848");
		/**
		 * 用户名
		 */
		private String username = "nacos";
		/**
		 * 密码
		 */
		private String password = "nacos";

		public Builder setServerAddress(List<String> serverAddresses) {
			this.serverAddresses = serverAddresses;
			return this;
		}

		public Builder setUsername(String username) {
			this.username = username;
			return this;
		}

		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}

		public NacosNameResolverProvider build() throws NacosException {
			List<NamingService> namingServices = new ArrayList<>(2);
			for (String serverAddress : serverAddresses) {
				Properties properties = new Properties();
				properties.put(PropertyKeyConst.SERVER_ADDR, serverAddress);
				properties.put(PropertyKeyConst.USERNAME, username);
				properties.put(PropertyKeyConst.PASSWORD, password);
				namingServices.add(NacosFactory.createNamingService(properties));
			}
			return new NacosNameResolverProvider(namingServices);
		}
	}
}
