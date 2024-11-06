package com.jindi.infra.logger.util;

import com.jindi.infra.common.util.InnerJSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * restful 客户端
 */
@Slf4j
public class RestClient {

	private static final Integer DEFAULT_CONNECT_TIMEOUT = 1000;
	private static final Integer DEFAULT_TIMEOUT = 5000;
	private final CloseableHttpClient client;

	public RestClient(String username, String password) {
		HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
				.setSocketTimeout(DEFAULT_TIMEOUT).setConnectionRequestTimeout(DEFAULT_TIMEOUT).build();
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
		UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(username, password);
		credentialsProvider.setCredentials(authScope, usernamePasswordCredentials);
		client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(poolingConnManager)
				.setDefaultCredentialsProvider(credentialsProvider)
				.build();
	}

	/**
	 * 获取内容
	 *
	 * @param uri          地址
	 * @param responseType 响应类型
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T getForObject(String uri, Class<T> responseType) throws IOException {
		return getForObject(uri, responseType, 0, 0);
	}

	/**
	 * 获取内容
	 *
	 * @param uri            地址
	 * @param responseType   响应类型
	 * @param connectTimeout 连接超时
	 * @param timeout        读写超时
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T getForObject(String uri, Class<T> responseType, Integer connectTimeout, Integer timeout)
			throws IOException {
		HttpGet httpGet = new HttpGet(uri);
		if (timeout <= 0) {
			timeout = DEFAULT_TIMEOUT;
		}
		if (connectTimeout <= 0) {
			connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		}
		httpGet.setConfig(RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(timeout)
				.setConnectionRequestTimeout(timeout).build());
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpGet);
			Objects.requireNonNull(httpResponse, "httpResponse require non null");
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode >= 400 || statusCode < 200) {
				throw new IOException(String.format("response status = %d", statusCode));
			}
			String data = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
			return InnerJSONUtils.parseObject(data, responseType);
		} catch (Throwable e) {
			log.error(String.format("getForObject uri = %s", uri), e);
			throw e;
		} finally {
			if (httpResponse != null) {
				httpResponse.close();
			}
		}
	}

	/**
	 * 创建对象
	 *
	 * @param uri          地址
	 * @param request      请求体
	 * @param responseType 响应类型
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T postForObject(String uri, Object request, Class<T> responseType) throws IOException {
		return postForObject(uri, request, responseType, 0, 0);
	}

	/**
	 * 创建对象
	 *
	 * @param uri            地址
	 * @param request        请求体
	 * @param responseType   响应类型
	 * @param connectTimeout 连接超时
	 * @param timeout        读写超时
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T postForObject(String uri, Object request, Class<T> responseType, Integer connectTimeout,
							   Integer timeout) throws IOException {
		HttpPost httpPost = new HttpPost(uri);
		if (request != null) {
			httpPost.setEntity(new StringEntity(InnerJSONUtils.toJSONString(request), ContentType.APPLICATION_JSON));
		}
		httpPost.setHeader("content-type", "application/json");
		if (connectTimeout <= 0) {
			connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		}
		if (timeout <= 0) {
			timeout = DEFAULT_TIMEOUT;
		}
		httpPost.setConfig(RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(timeout)
				.setConnectionRequestTimeout(timeout).build());
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpPost);
			Objects.requireNonNull(httpResponse, "httpResponse require non null");
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode >= 400 || statusCode < 200) {
				throw new IOException(String.format("response status = %d", statusCode));
			}
			if (responseType == Void.class) {
				return null;
			}
			String data = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
			if (CharSequence.class.isAssignableFrom(responseType)) {
				return (T) data;
			}
			if (StringUtils.isBlank(data)) {
				return null;
			}
			return InnerJSONUtils.parseObject(data, responseType);
		} catch (Throwable e) {
			log.debug(String.format("postForObject uri = %s request = %s", uri, InnerJSONUtils.toJSONString(request)), e);
			throw e;
		} finally {
			if (httpResponse != null) {
				httpResponse.close();
			}
		}
	}
}
