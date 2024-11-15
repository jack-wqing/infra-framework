package com.jindi.infra.feign.client;

import static com.zspace.infra.metrics.cat.constant.CatType.FEIGN_CALL;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;

public class CatFeignClientDecorator implements Client {

	private final Client delegate;

	private final String PARAM_SPLIT = "?";

	public CatFeignClientDecorator(Client delegate) {
		this.delegate = delegate;
	}

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {
		String url = getUrl(request);

		Transaction feignTransaction = Cat.newTransaction(FEIGN_CALL, url);
		try {
			Response response = delegate.execute(request, options);
			feignTransaction.setStatus(Message.SUCCESS);
			return response;
		} catch (Throwable e) {
			// catch 到异常，设置状态，代表此请求失败
			feignTransaction.setStatus(e);
			// 将异常上报到cat上
			Cat.logError(e);
			throw e;
		} finally {
			feignTransaction.complete();
		}
	}

	private String getUrl(Request request) {
		RequestTemplate requestTemplate = request.requestTemplate();
		String url;
		if (requestTemplate == null || StringUtils.isBlank(requestTemplate.url())) {
			url = request.url();
		} else {
			url = requestTemplate.url();
		}

		if (StringUtils.contains(url, PARAM_SPLIT)) {
			url = url.substring(0, url.indexOf(PARAM_SPLIT));
		}

		return url;
	}
}
