package com.jindi.infra.biz.context.filler;


import com.jindi.common.tools.context.TycRequestContext;
import com.jindi.common.tools.util.ContextUtil;
import com.jindi.infra.common.util.InnerJSONUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static com.jindi.common.tools.constant.ContextConstant.REQUEST_CONTEXT_KEY;

public class TycRequestContextFiller implements RequestContextFiller {

    @Override
    public void fill(HttpServletRequest request) {
        String value = request.getHeader(REQUEST_CONTEXT_KEY);
        if (StringUtils.isNotBlank(value)) {
            TycRequestContext requestContext = InnerJSONUtils.parseObject(value, TycRequestContext.class);
            ContextUtil.setRequestContext(requestContext);
        }
    }
}
