package com.jindi.infra.logger;


import com.jindi.infra.logger.logger.TycLogDataInterface;

import java.util.HashMap;
import java.util.Map;

public class UserIdTycLoggerCustomImpl implements TycLogDataInterface {

    @Override
    public Map<String, Object> get() {
        Map<String, Object> map = new HashMap<>();
        //从上下文获取用户id
        map.put("userId", 123456);
        return map;
    }
}
