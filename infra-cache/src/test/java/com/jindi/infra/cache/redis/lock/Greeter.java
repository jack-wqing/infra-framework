package com.jindi.infra.cache.redis.lock;

import com.jindi.infra.cache.redis.lock.annotation.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Greeter {

    @DistributedLock(keyExpression = "#args[1]", timeoutMillis = 10000, block = true)
    public void hi(String name, Integer no) throws InterruptedException {
        Thread.sleep(50);
        log.info("hello: {}", name);
    }
}
