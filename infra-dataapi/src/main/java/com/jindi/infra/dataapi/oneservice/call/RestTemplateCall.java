package com.jindi.infra.dataapi.oneservice.call;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts;
import com.jindi.infra.dataapi.oneservice.param.OneServiceParam;
import com.jindi.infra.dataapi.spacecloud.OneServiceException;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static com.jindi.infra.dataapi.oneservice.constant.OneServiceConsts.X_AUTH_TOKEN;

public class RestTemplateCall {

    @Autowired
    private ObjectProvider<CollectorRegistry> collectorRegistryObjectProvider;
    private Counter counter;
    private Gauge gauge;

    @Value("${spring.application.name}")
    private String application;
    @Value("${oneservice.config.auth.token:}")
    private String token;

    private final RestTemplate restTemplate;

    /**
     * 构造方法注入，避免注入其他的restTemplate实现类
     */
    public RestTemplateCall(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initMethod() {
        CollectorRegistry collectorRegistry = collectorRegistryObjectProvider.getIfAvailable();
        if (collectorRegistry == null) {
            return;
        }
        Counter.Builder counterBuilder = Counter.build()
                .namespace(OneServiceConsts.ONE_SERVICE)
                .subsystem(OneServiceConsts.CLIENT)
                .name(OneServiceConsts.COUNT)
                .labelNames(OneServiceConsts.APPLICATION, OneServiceConsts.RESOURCE, OneServiceConsts.PROJECT, OneServiceConsts.FOLDER, OneServiceConsts.API, OneServiceConsts.VERSION)
                .help("oneService客户端调用计数");
        counter = counterBuilder.register(collectorRegistry);
        Gauge.Builder gaugeBuilder = Gauge.build()
                .namespace(OneServiceConsts.ONE_SERVICE)
                .subsystem(OneServiceConsts.CLIENT)
                .name(OneServiceConsts.RESPONSE_TIME)
                .labelNames(OneServiceConsts.APPLICATION, OneServiceConsts.RESOURCE, OneServiceConsts.PROJECT, OneServiceConsts.FOLDER, OneServiceConsts.API, OneServiceConsts.VERSION)
                .help("oneService客户端调用耗时");
        gauge = gaugeBuilder.register(collectorRegistry);
    }

    public String call(String urlPrefix, String invokePath, String project, String folder, String api, String version, String body) {
        Gauge.Timer timer = null;
        try (Entry ignored = SphU.entry(invokePath)) {
            timer = prometheusMetrics(urlPrefix, project, folder, api, version);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(X_AUTH_TOKEN, token);
            HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(urlPrefix + invokePath, httpEntity, String.class);
        } catch (BlockException e) {
            throw new OneServiceException("请求限流 path: " + invokePath, e);
        } finally {
            prometheusTimer(timer);
        }
    }

    private void prometheusTimer(Gauge.Timer timer) {
        if (timer != null) {
            timer.setDuration();
        }
    }

    private Gauge.Timer prometheusMetrics(String urlPrefix, String project, String folder, String api, String version) {
        Gauge.Timer timer = null;
        if (counter != null) {
            counter.labels(application, urlPrefix, project, folder, api, version).inc();
        }
        if (gauge != null) {
            timer = gauge.labels(application, urlPrefix, project, folder, api, version).startTimer();
        }
        return timer;
    }
}
