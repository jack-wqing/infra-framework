package com.jindi.infra.cache.redis.lock;

import com.jindi.infra.cache.redis.autoconfig.CacheEnhanceAutoConfiguration;
import com.jindi.infra.cache.redis.tycredis.TycRedisClientTestsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest(classes = {TycRedisClientTestsConfig.class, LockConfig.class})
@TestPropertySource("classpath:redis-test.properties")
@Slf4j
class DistributedLockTests {

    @Autowired
    private Greeter greeter;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Test
    void testDistributedLock() throws ExecutionException, InterruptedException {
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Future future = executorService.submit(() -> {
                try {
                    int no = RandomUtils.nextInt(0, 10);
                    greeter.hi(String.format("XinCao-%d", no), no);
                } catch (Throwable e) {
                    log.error("", e);
                }
            });
            futures.add(future);
        }
        for (Future future : futures) {
            future.get();
        }
    }
}
