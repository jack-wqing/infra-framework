package com.jindi.infra.topology.http.filter;

import com.jindi.infra.topology.consts.TopologyConst;
import com.jindi.infra.topology.model.TopologyEsWriter;
import com.jindi.infra.topology.utils.TopologyHeaderUtil;
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


@Slf4j
public class TopologyHttpFilter implements Filter {
    @Value("${http.filter.uri.path:/checkAlive,/favicon.ico}")
    private HashSet<String> filterUriPath;
    @Resource
    private TopologyEsWriter topologyEsWriter;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            if (!filterUriPath.contains(request.getRequestURI())) {
                String headerValue = request.getHeader(TopologyConst.HEADER_CHAIN_KEY);
                TopologyHeaderUtil.copyHeaderToLocal(headerValue);
            }
        } catch (Throwable e) {
            if (topologyEsWriter != null) {
                topologyEsWriter.writeException(e, "httpServer");
            }
        }
        try {
            chain.doFilter(servletRequest, servletResponse);
        } catch (Throwable e) {
            throw e;
        } finally {
            TopologyHeaderUtil.cleanLocal();
        }
    }

}
