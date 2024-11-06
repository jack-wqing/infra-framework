package com.jindi.infra.biz.context;

import static com.jindi.common.tools.constant.ContextConstant.REQUEST_CONTEXT_KEY;

import org.apache.commons.lang3.StringUtils;

import com.jindi.common.tools.context.TycRequestContext;
import com.jindi.common.tools.util.ContextUtil;
import com.jindi.infra.common.util.InnerJSONUtils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestContextFeignCallInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		TycRequestContext requestContext = ContextUtil.getRequestContext();
		String context = InnerJSONUtils.toJSONString(requestContext);
		if (StringUtils.isNotBlank(context) && !context.equals("{}")) {
			requestTemplate.header(REQUEST_CONTEXT_KEY, context);
		}
	}
}
