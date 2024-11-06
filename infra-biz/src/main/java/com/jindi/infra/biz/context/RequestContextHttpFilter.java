package com.jindi.infra.biz.context;

import static com.jindi.common.tools.constant.ContextConstant.LOCATION_CONTEXT_KEY;
import static com.jindi.common.tools.constant.ContextConstant.REQUEST_CONTEXT_KEY;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncoder;
import com.dianping.cat.Cat;
import com.jindi.common.tools.context.LocationContext;
import com.jindi.infra.biz.context.filler.RequestContextFiller;
import org.apache.commons.lang3.StringUtils;

import com.jindi.common.tools.context.TycRequestContext;
import com.jindi.common.tools.util.ContextUtil;
import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public class RequestContextHttpFilter implements Filter {

	private List<RequestContextFiller> fillers;

	public RequestContextHttpFilter(List<RequestContextFiller> fillers) {
		this.fillers = fillers;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		if (CollectionUtils.isEmpty(fillers)) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}
		try {
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			fillers.forEach(filler -> filler.fill(request));
			filterChain.doFilter(servletRequest, servletResponse);
		} catch (Exception e) {
			throw e;
		} finally {
			ContextUtil.remove();
		}
	}


}
