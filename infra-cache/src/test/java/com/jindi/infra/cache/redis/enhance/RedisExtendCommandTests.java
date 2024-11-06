package com.jindi.infra.cache.redis.enhance;

import com.jindi.infra.cache.redis.autoconfig.CacheEnhanceAutoConfiguration;
import com.jindi.infra.cache.redis.tycredis.TycRedisClientTestsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = {TycRedisClientTestsConfig.class, CacheEnhanceAutoConfiguration.class})
@TestPropertySource("classpath:redis-test.properties")
@Slf4j
class RedisExtendCommandTests {

    private static final Map<String, String> KEY_VALUES = new HashMap<>();
    private static final Integer TOTAL = 100000;

    static {
        for (int i = 0; i < 100; i++) {
            KEY_VALUES.put(RandomStringUtils.randomAlphanumeric(32), RandomStringUtils.randomAlphanumeric(32));
        }
    }

    @Autowired
    private RedisExtendCommand redisExtendCommand;

    @Test
    void testMultiSetExpire() {
        long startTime = System.nanoTime();
        for (int no = 0; no < TOTAL; no++) {
            redisExtendCommand.multiSetExpire(KEY_VALUES, 30000);
        }
        long costTime = System.nanoTime() - startTime;
        log.info("multiSetExpire 平均耗时 {} 毫秒， QPS {} 次/秒", costTime * 1.0 / TOTAL / 1000 / 1000, TOTAL / (costTime * 1.0 / 1000 / 1000 / 1000));
    }

    @Test
    void multiSet() {
        long startTime = System.nanoTime();
        for (int no = 0; no < TOTAL; no++) {
            redisExtendCommand.multiSet(KEY_VALUES);
        }
        long costTime = System.nanoTime() - startTime;
        log.info("multiSet 平均耗时 {} 毫秒， QPS {} 次/秒", costTime * 1.0 / TOTAL / 1000 / 1000, TOTAL / (costTime * 1.0 / 1000 / 1000 / 1000));
    }

}
