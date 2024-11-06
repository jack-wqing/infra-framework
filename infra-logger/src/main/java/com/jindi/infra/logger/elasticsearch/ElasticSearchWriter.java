package com.jindi.infra.logger.elasticsearch;

import com.jindi.infra.common.util.InnerDateUtils;
import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.core.model.FrameworkLogEvent;
import com.jindi.infra.logger.util.RestClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 索引名格式：infra-error-log-2022-04-22
 * <pre>
 *     {
 *   "index_patterns": [
 *     "infra-error-log*"
 *   ],
 *   "mappings": {
 *     "properties": {
 *       "application": {
 *         "type": "text"
 *       },
 *       "type": {
 *         "type": "keyword"
 *       },
 *       "name": {
 *         "type": "keyword"
 *       },
 *       "env": {
 *         "type": "keyword"
 *       },
 *       "ip": {
 *         "type": "keyword"
 *       },
 *       "message": {
 *         "type": "text"
 *       },
 *       "timestamp": {
 *         "type": "date"
 *       }
 *     }
 *   }
 * }
 * </pre>
 */
@Slf4j
public class ElasticSearchWriter implements EnvironmentAware, ApplicationListener<FrameworkLogEvent> {

    private static final String INFRA_ERROR_LOG = "infra-error-log";
    private static final String SPRING_APPLICATION_NAME = "spring.application.name";
    private static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";
    private static final String LOGGER_ELASTICSEARCH_ADDRESS = "logger.elasticsearch.address";
    private static final String HTTP_S_S_DOC = "http://%s/%s-%s/_doc";
    private static final String LOGGER_ELASTICSEARCH_USERNAME = "logger.elasticsearch.username";
    private static final String LOGGER_ELASTICSEARCH_PASSWORD = "logger.elasticsearch.password";
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1,
            1,
            0,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.DiscardPolicy());
    private Environment environment;
    private String address;
    private String application;
    private String env;
    private String ip;
    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.address = environment.getProperty(LOGGER_ELASTICSEARCH_ADDRESS);
        this.application = environment.getProperty(SPRING_APPLICATION_NAME);
        this.env = environment.getProperty(SPRING_PROFILES_ACTIVE);
        this.ip = InnerIpUtils.getCachedIP();
        String username = environment.getProperty(LOGGER_ELASTICSEARCH_USERNAME, "");
        String password = environment.getProperty(LOGGER_ELASTICSEARCH_PASSWORD, "");
        // this.restClient = new RestClient(username, password);
    }

    /**
     * @param message
     */
    public void write(String type, String name, String message) {
        // write(INFRA_ERROR_LOG, type, name, message);
    }

    /**
     * @param topic
     * @param message
     */
    public void write(String topic, String type, String name, String message) {
//        Event event = Event.builder()
//                .application(application)
//                .type(type)
//                .name(name)
//                .env(env)
//                .ip(ip)
//                .timestamp(System.currentTimeMillis())
//                .message(message)
//                .build();
//        try {
//            executor.submit(() -> {
//                try {
//                    restClient.postForObject(String.format(HTTP_S_S_DOC, address, topic, InnerDateUtils.nowDateString()), event, Void.class);
//                } catch (IOException e) {
//                    log.debug("elasticsearch write:", e);
//                }
//            });
//        } catch (Throwable e) {
//            log.debug("elasticsearch submit:", e);
//        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(FrameworkLogEvent event) {
        if (StringUtils.isAnyBlank(event.getName(), event.getType(), event.getMessage())) {
            return;
        }
        write(event.getType(), event.getName(), event.getMessage());
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Event {
        private String application;
        private String type;
        private String name;
        private String env;
        private String ip;
        private String message;
        private Long timestamp;
    }
}
