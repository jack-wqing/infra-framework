package com.jindi.infra.feign.filter;

import com.google.gson.reflect.TypeToken;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.aspect.MultiProtocolClientInterceptor;
import com.jindi.infra.core.constants.HeaderConsts;
import com.jindi.infra.feign.constant.FeignConsts;
import com.jindi.infra.core.aspect.MultiProtocolServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@WebFilter(filterName = "feignServerMultiProtocolFilter", urlPatterns = {"*"})
@Slf4j
public class FeignServerMultiProtocolFilter implements Filter {

    private final List<MultiProtocolServerInterceptor> interceptorList;

    public FeignServerMultiProtocolFilter(List<MultiProtocolServerInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("-------FeignServerMultiProtocolFilter init--------");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (!isFeignRequest(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String path = request.getRequestURI();
        Map<String, String> headers = getHeaders(request);
        for (MultiProtocolServerInterceptor interceptor : interceptorList) {
            interceptor.before(path, headers);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable e) {
            throw e;
        } finally {
            for (MultiProtocolServerInterceptor interceptor : interceptorList) {
                interceptor.after(path);
            }
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        String value = request.getHeader(HeaderConsts.INFRA_CONTEXT_HEADER_KEY);
        if (StringUtils.isNotBlank(value)) {
            headers = InnerJSONUtils.parseObject(value, new TypeToken<HashMap<String, String>>() {}.getType());
        }
        for (String key : HeaderConsts.BUSINESS_HEADER_KEY_LIST) {
            value = request.getHeader(key);
            if (StringUtils.isNotBlank(value)) {
                headers.put(key, value);
            }
        }
        return headers;
    }

    private Boolean isFeignRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (Objects.equals(FeignConsts.ORIGIN, cookie.getName())
                    && Objects.equals(FeignConsts.FEIGN, cookie.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        log.info("-------FeignServerMultiProtocolFilter destroy--------");
    }
}