package com.jindi.infra.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * restful 客户端
 */
@Slf4j
public class InnerRestClientUtils {

	private static final CloseableHttpClient client;
	private static final Integer DEFAULT_CONNECT_TIMEOUT = 1000;
	private static final Integer DEFAULT_TIMEOUT = 5000;

	static {
		HttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
				.setSocketTimeout(DEFAULT_TIMEOUT).setConnectionRequestTimeout(DEFAULT_TIMEOUT).build();
		client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(poolingConnManager)
				.build();
	}

	/**
	 * 获取内容
	 *
	 * @param uri
	 *            地址
	 * @param responseType
	 *            响应类型
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public static <T> T getForObject(String uri, Class<T> responseType) throws IOException {
		return getForObject(uri, responseType, 0, 0);
	}

	public static <T> T getForObject(String uri, Map<String, Object> params, Class<T> responseType) throws IOException, URISyntaxException {
		URI u = URI.create(uri);
		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(u.getQuery())) {
			query.append(u.getQuery());
		}
		if (!CollectionUtils.isEmpty(params)) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (query.length() > 0) {
					query.append("&");
				}
				query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString())).append("=")
						.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.toString()));
			}
		}
		URI newURI = new URI(u.getScheme(), u.getAuthority(), u.getPath(), query.toString(), u.getFragment());
		return getForObject(newURI.toASCIIString(), responseType, 0, 0);
	}

	/**
	 * 获取内容
	 *
	 * @param uri
	 *            地址
	 * @param responseType
	 *            响应类型
	 * @param connectTimeout
	 *            连接超时
	 * @param timeout
	 *            读写超时
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public static <T> T getForObject(String uri, Class<T> responseType, Integer connectTimeout, Integer timeout)
			throws IOException {
		HttpGet httpGet = new HttpGet(uri);
		if (timeout > 0) {
			if (connectTimeout <= 0) {
				connectTimeout = DEFAULT_CONNECT_TIMEOUT;
			}
			httpGet.setConfig(RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(timeout)
					.setConnectionRequestTimeout(timeout).build());
		}
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpGet);
			Objects.requireNonNull(httpResponse, "httpResponse require non null");
			if (!Objects.equals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK)) {
				throw new IOException(String.format("uri = %s, response status = %d", uri,
						httpResponse.getStatusLine().getStatusCode()));
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
	 * @param uri
	 *            地址
	 * @param request
	 *            请求体
	 * @param responseType
	 *            响应类型
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public static <T> T postForObject(String uri, Object request, Class<T> responseType) throws IOException {
		return postForObject(uri, request, responseType, 0, 0);
	}

	/**
	 * 创建对象
	 *
	 * @param uri
	 *            地址
	 * @param request
	 *            请求体
	 * @param responseType
	 *            响应类型
	 * @param connectTimeout
	 *            连接超时
	 * @param timeout
	 *            读写超时
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public static <T> T postForObject(String uri, Object request, Class<T> responseType, Integer connectTimeout,
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
