package com.jindi.infra.cache.redis.metrics;


import com.jindi.infra.cache.redis.properties.TycRedisHolder;
import com.jindi.infra.cache.redis.properties.TycRedisProperties;
import com.jindi.infra.cache.redis.utils.RedisConnectInfoUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseTycRedisClientMetrics implements MeterBinder {

    @Autowired(required = false)
    private List<RedisTemplate> redisTemplates;

    @Autowired(required = false)
    private TycRedisProperties tycRedisProperties;

    private Map<String, String> holderMap;

    private Iterable<Tag> tags;

    public BaseTycRedisClientMetrics() {
        this(Collections.emptyList());
    }

    public BaseTycRedisClientMetrics(Iterable<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public final void bindTo(MeterRegistry registry) {
        if (CollectionUtils.isEmpty(redisTemplates)) {
            log.debug("数据源配置为空");
        } else {
            for (RedisTemplate redisTemplate : redisTemplates) {
                binDataSourceMetrics(redisTemplate, registry);
            }
        }
    }

    private void binDataSourceMetrics(RedisTemplate redisTemplate, MeterRegistry registry) {
        String hostName = getHostName(redisTemplate);
        String port = getPort(redisTemplate);
        String database = getDatabase(redisTemplate);
        if (StringUtils.isBlank(hostName)) {
            return;
        }
        String template = RedisConnectInfoUtils.getConnectInfo(hostName, port, database);
        if (redisTemplate != null) {
            Counter.builder("tyc.redis.relation").tags(Tags.concat(tags,
                    new String[]{"redis_client", getBeanName(template), "redis_host", hostName, "database", database}))
                    .description("").register(registry);
        }
    }


    public abstract String getHostName(RedisTemplate redisTemplate);
    public abstract String getPort(RedisTemplate redisTemplate);
    public abstract String getDatabase(RedisTemplate redisTemplate);
    public abstract String getConnection(RedisTemplate redisTemplate);

    private String getBeanName(String template) {
        if (holderMap == null) {
            holderMap = new HashMap<>();
            if (tycRedisProperties != null) {
                for (Map.Entry<String, TycRedisHolder> entry : tycRedisProperties.entrySet()) {
                    holderMap.put(entry.getValue().getConnectInfo(), entry.getKey());
                }
            }
        }
        return holderMap.getOrDefault(template, "unknown");
    }
}
