package com.jindi.infra.biz.context.filler;


import cn.hutool.core.net.URLDecoder;
import com.dianping.cat.Cat;
import com.jindi.common.tools.context.LocationContext;
import com.jindi.common.tools.context.TycRequestContext;
import com.jindi.common.tools.util.ContextUtil;
import com.jindi.infra.common.util.InnerJSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.Charset;

import static com.jindi.common.tools.constant.ContextConstant.LOCATION_CONTEXT_KEY;
import static com.jindi.common.tools.constant.ContextConstant.REQUEST_CONTEXT_KEY;

@Slf4j
public class LocationRequestContextFiller implements RequestContextFiller {

    private static final String REAL_LOCATION_CONTEXT_KEY = "real_location_context";

    @Override
    public void fill(HttpServletRequest request) {
        String value = request.getHeader(LOCATION_CONTEXT_KEY);
        if (StringUtils.isBlank(value)) {
            value = request.getHeader(REAL_LOCATION_CONTEXT_KEY);
        }
        if (StringUtils.isNotBlank(value)) {
            try {
                LocationContext locationContext = InnerJSONUtils.parseObject(URLDecoder.decode(value, Charset.defaultCharset()), LocationContext.class);
                ContextUtil.setLocationContext(locationContext);
            } catch (Exception e) {
                log.error("解析LocationContext失败", e);
                Cat.logEvent("解析LocationContext失败", request.getRequestURI());
            }
        }
    }
}
