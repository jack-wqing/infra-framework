package com.jindi.infra.feign.filter;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.jindi.infra.feign.constant.FeignConsts;
import com.zspace.infra.metrics.cat.constant.CatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

@WebFilter(filterName = "feignRequestFilter", urlPatterns = {"*"})
public class FeignRequestFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FeignRequestFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("-------feign filter init--------");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (!isFeignRequest(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        Transaction transaction = Cat.newTransaction(CatType.FEIGN_SERVICE, request.getRequestURI());
        try {
            Cat.logEvent(CatType.FEIGN_UPSTREAM, String.format("%s:%s", request.getRemoteAddr(), request.getRequestURI()));
            filterChain.doFilter(servletRequest, servletResponse);
            transaction.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            // catch 到异常，设置状态，代表此请求失败
            transaction.setStatus("ERROR");
            // 将异常上报到cat上
            Cat.logError(e);
            throw e;
        } finally {
            transaction.complete();
        }
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
        logger.info("-------feign filter destroy--------");
    }
}