package com.jindi.infra.logger;

import cn.hutool.core.lang.Pair;
import com.jindi.infra.logger.logger.TycLogger;
import com.jindi.infra.logger.loggerFactory.TycLoggerFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class DemoService {

    private static final TycLogger LOGGER = TycLoggerFactory.getLogger(DemoService.class);

    public void doSomeThing(Long userId) {
        RuntimeException exception = new RuntimeException("测试");

        LOGGER.info("开始执行, userId:{}", userId);
        LOGGER.info("开始执行, userId:{}", "abc");

        LOGGER.info(TycLogger.toMap( "age", "18", "name", "zhangsan"), "开始执行, userId:{}", userId);

        LOGGER.info(new DemoVO("1", "zhangsan"), "开始执行, userId:{}", userId);

        LOGGER.info(Pair.of("age", "18"), Pair.of("name", "zhangsan"), Pair.of("msg", "开始执行"));

        LOGGER.info(TycLogger.toMap(Pair.of("age", "18"), Pair.of("name", "zhangsan"), Pair.of("msg", "开始执行")), "开始执行, userId:{}", userId);

        LOGGER.error("开始执行, userId:{}", userId, exception);

        LOGGER.error(TycLogger.toMap( "age", "18", "name", "zhangsan"), "开始执行, userId:{}", userId, exception);

        LOGGER.error(new DemoVO("1", "zhangsan"), "开始执行, userId:{}", userId, exception);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DemoVO {
        private String id;
        private String name;
    }
}
