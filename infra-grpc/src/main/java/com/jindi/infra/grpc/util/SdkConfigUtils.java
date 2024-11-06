package com.jindi.infra.grpc.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.core.exception.RpcClientException;

import io.grpc.BindableService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SdkConfigUtils {

	/**
	 * 缓存服务端sdk jar包application.properties路径 -> applicationName
	 */
	private static final Map<String, String> serverNameCache = Collections.synchronizedMap(new HashMap<>());

	public static String parseServerName(Class<BindableService> clazz) throws Exception {
		URL location = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class");
		String path = location.getPath();
		String serverNamePath = getServerNamePath(path);
		if (serverNamePath == null) {
			throw new RpcClientException(clazz.getSimpleName() + " is not in remote sdk jar");
		}
		String serverName = serverNameCache.get(serverNamePath);
		if (StringUtils.isNotBlank(serverName))
			return serverName;
		serverName = loadServerName(serverNamePath);
		if (StringUtils.isNotBlank(serverName)) {
			serverNameCache.put(serverNamePath, serverName);
			return serverName;
		}
		throw new RpcClientException(serverNamePath + " rpc.serverName property is null");
	}

	private static String loadServerName(String serverNamePath) throws RpcClientException {
		InputStream inputStream = null;
		try {
			URL newUrl = new URL(serverNamePath);
			Properties prop = new Properties();
			inputStream = newUrl.openStream();
			prop.load(inputStream);
			return prop.getProperty("rpc.serverName");
		} catch (Exception e) {
			throw new RpcClientException("load " + serverNamePath + " error", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}

	private static String getServerNamePath(String path) {
		if (StringUtils.isNotBlank(path) && path.contains("!")) {
			return "jar:" + path.substring(0, path.lastIndexOf("!") + 1) + "/META-INF/application.properties";
		} else if (StringUtils.isNotBlank(path) && StringUtils.contains(path, "/target/test-classes/")) {
			return "file:" + StringUtils.substringBeforeLast(path, "test-classes")
					+ "test-classes/META-INF/application.properties";
		} else if (StringUtils.isNotBlank(path) && StringUtils.contains(path, "/target/classes/")) {
			return "file:" + StringUtils.substringBeforeLast(path, "classes")
					+ "classes/META-INF/application.properties";
		}
		return null;
	}
}
