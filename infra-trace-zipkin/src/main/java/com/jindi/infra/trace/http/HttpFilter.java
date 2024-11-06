package com.jindi.infra.trace.http;

import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.http.context.HttpTraceContext;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.jindi.infra.trace.utils.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;

/**
 * 普通请求TraceFilter，生成或继承trace信息
 * @author changbo <changbo@kuaishou.com>
 * Created on 2020-03-02
 */
@Slf4j
public class HttpFilter implements Filter {

    @Resource
    private HttpTraceContext httpTraceContext;
    @Resource
    private TraceContext traceContext;
    @Value("${http.filter.uri.path:/checkAlive,/favicon.ico}")
    private HashSet<String> filterUriPath;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (filterUriPath.contains(request.getRequestURI())) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        Span span = httpTraceContext.buildServerTraceSpan(request);
        TraceUtil.tagCatMessageId(span);
        try {
            chain.doFilter(servletRequest, servletResponse);
        } catch (Throwable e) {
            TraceUtil.tag(span, TagsConsts.ERROR, e.getMessage());
            throw e;
        } finally {
            traceContext.writeSpan(span);
            TraceMDCUtil.clean();
        }
    }
}
