package com.jindi.infra.space.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.space.SpaceCloudException;
import com.jindi.infra.space.SpaceCloudUrlLocator;
import com.jindi.infra.space.constant.GraphQLTypeEnums;
import com.jindi.infra.space.constant.SpaceCloudConsts;
import com.jindi.infra.space.param.SpaceCloudParam;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpaceCloudHttpClient extends AbstractGraphQLHttpClient {

    private static final char BRACKET = '[';
    private static final String X_REQUEST_ID = "x-request-id";
    private static final String APPLICATION = "application";
    private final RestTemplate restTemplate;
    @Resource
    private SpaceCloudUrlLocator spaceCloudUrlLocator;
    private Counter counter;
    private Gauge gauge;
    @Autowired
    private ObjectProvider<CollectorRegistry> collectorRegistryObjectProvider;
    @Value("${spring.application.name}")
    private String application;

    /**
     * 构造方法注入，避免注入其他的restTemplate实现类
     *
     * @param restTemplate
     */
    public SpaceCloudHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initMethod() {
        CollectorRegistry collectorRegistry = collectorRegistryObjectProvider.getIfAvailable();
        if (collectorRegistry == null) {
            return;
        }
        Counter.Builder counterBuilder = Counter.build()
                .namespace(SpaceCloudConsts.SPACE_CLOUD)
                .subsystem(SpaceCloudConsts.CLIENT)
                .name(SpaceCloudConsts.COUNT)
                .labelNames(SpaceCloudConsts.APPLICATION, SpaceCloudConsts.RESOURCE, SpaceCloudConsts.PROJECT, SpaceCloudConsts.SERVICE, SpaceCloudConsts.ENDPOINT)
                .help("space-cloud 客户端调用计数");
        counter = counterBuilder.register(collectorRegistry);
        Gauge.Builder gaugeBuilder = Gauge.build()
                .namespace(SpaceCloudConsts.SPACE_CLOUD)
                .subsystem(SpaceCloudConsts.CLIENT)
                .name(SpaceCloudConsts.RESPONSE_TIME)
                .labelNames(SpaceCloudConsts.APPLICATION, SpaceCloudConsts.RESOURCE, SpaceCloudConsts.PROJECT, SpaceCloudConsts.SERVICE, SpaceCloudConsts.ENDPOINT)
                .help("space-cloud 客户端调用耗时");
        gauge = gaugeBuilder.register(collectorRegistry);
    }

    public String query(String project, String service, String endpoint, String body) {
        String urlPrefix = matchUrlPrefix(project, service, endpoint);
        if (StringUtils.isBlank(urlPrefix)) {
            return null;
        }
        String url = urlPrefix + "/" + project + "/services/" + service + "/" + endpoint;
        Gauge.Timer timer = null;
        try (Entry ignored = SphU.entry(url)) {
            if (gauge != null) {
                timer = gauge.labels(application, urlPrefix, project, service, endpoint).startTimer();
            }
            if (counter != null) {
                counter.labels(application, urlPrefix, project, service, endpoint).inc();
            }
            log.debug("space-cloud url: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            TracePropagation tracePropagation = TraceMDCUtil.getCurrentTracePropagation();
            if (tracePropagation != null && StringUtils.isNotBlank(tracePropagation.getTraceId())) {
                headers.add(X_REQUEST_ID, tracePropagation.getTraceId());
            }
            headers.add(APPLICATION, application);
            HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(url, httpEntity, String.class);
        } catch (BlockException ex) {
            throw new SpaceCloudException(ex);
        } finally {
            if (timer != null) {
                timer.setDuration();
            }
        }
    }

    /**
     * 获取实体列表
     *
     * @param responseType
     * @param body
     * @param <T>
     * @return
     */
    public <T> List<T> getList(Class<T> responseType, Object body) {
        String data = getRaw(body);
        if (StringUtils.isBlank(data)) {
            return Collections.emptyList();
        }
        if (data.charAt(0) == BRACKET) {
            return JSON.parseArray(data, responseType);
        }
        Map<String, Object> map = InnerJSONUtils.parseMap(data);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            return JSON.parseArray(InnerJSONUtils.toJSONString(entry.getValue()), responseType);
        }
        return Collections.emptyList();
    }

    /**
     * 获取一个实体
     *
     * @param responseType
     * @param body
     * @param <T>
     * @return
     */
    public <T> T getOne(Class<T> responseType, Object body) {
        List<T> list = getList(responseType, body);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取完整的实体
     *
     * @param responseType
     * @param body
     * @param <T>
     * @return
     */
    public <T> T get(Class<T> responseType, Object body) {
        String data = getRaw(body);
        if (data == null) {
            return null;
        }
        return InnerJSONUtils.parseObject(data, responseType);
    }

    /**
     * 获取实体的字符串内容
     *
     * @param body
     * @return
     */
    public String getRaw(Object body) {
        SpaceCloudParam spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
        if (spaceCloudParam == null) {
            throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
        }
        logEvent("getRaw", body);
        return query(spaceCloudParam.getProject(), spaceCloudParam.getService(), spaceCloudParam.getEndpoint(), InnerJSONUtils.toJSONString(body));
    }

    @Override
    public GraphQLTypeEnums getGraphQLTypeEnums() {
        return GraphQLTypeEnums.SPACE_CLOUD;
    }

    /**
     * 按路径深度匹配url前缀
     */
    @Override
    public String matchUrlPrefix(String project, String service, String endpoint) {
        String key = project + "#" + service + "#" + endpoint;
        String urlPrefix = matchUrlPrefix(key);
        if (urlPrefix != null) return urlPrefix;
        key = project + "#" + service;
        urlPrefix = matchUrlPrefix(key);
        if (urlPrefix != null) return urlPrefix;
        key = project;
        urlPrefix = matchUrlPrefix(key);
        if (urlPrefix != null) return urlPrefix;
        throw new SpaceCloudException("project: " + project + " 需要关联 SpaceCloud 集群名");
    }

    private String matchUrlPrefix(String key) {
        String urlPrefix = spaceCloudUrlLocator.getUrl(key);
        if (StringUtils.isNotBlank(urlPrefix)) {
            log.debug("匹配 endpoint: {}", key);
            return urlPrefix;
        }
        return null;
    }
}