package com.jindi.infra.governance.lane;

import com.jindi.infra.core.constants.LaneTagThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class LaneFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        LaneTagThreadLocal.clearLaneTag();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String value = getTag(request);
        if (StringUtils.isNotBlank(value)) {
            String path = request.getRequestURI();
            log.debug("接收到来自泳道的请求, tag:{}, path:{}", value, path);
            LaneTagThreadLocal.saveLaneTag(value);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String getTag(HttpServletRequest request) {
        String value = request.getHeader(LaneTagThreadLocal.LANE_TAG_KEY);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return request.getHeader(LaneTagThreadLocal.LANE_TAG_KEY_LOWER_CASE);
    }
}
