package com.jindi.infra.governance.lane.grpc;


import com.jindi.infra.core.aspect.CoreRpcServerInterceptor;
import com.jindi.infra.core.constants.LaneTagThreadLocal;
import com.jindi.infra.core.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class LaneGrpcCoreServerInterceptor implements CoreRpcServerInterceptor {
    @Override
    public void before(String className, String methodName, Object... params) {
        String laneTag = getTag();
        if (StringUtils.isNotBlank(laneTag)) {
            log.debug("接收到来自泳道的请求, tag:{}, method:{}", laneTag, className + "." + methodName);
            LaneTagThreadLocal.saveLaneTag(laneTag);
        }
    }

    @Override
    public void after(String className, String methodName, Object response, Object... params) {
        LaneTagThreadLocal.clearLaneTag();
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private String getTag() {
        String value = ContextUtils.getContextValue(LaneTagThreadLocal.LANE_TAG_KEY);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return ContextUtils.getContextValue(LaneTagThreadLocal.LANE_TAG_KEY_LOWER_CASE);
    }
}
