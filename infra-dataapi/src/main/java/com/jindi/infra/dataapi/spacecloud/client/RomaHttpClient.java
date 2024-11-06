package com.jindi.infra.dataapi.spacecloud.client;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSONObject;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.dataapi.spacecloud.SpaceCloudException;
import com.jindi.infra.dataapi.spacecloud.constant.GraphQLTypeEnums;
import com.jindi.infra.dataapi.spacecloud.constant.SpaceCloudConsts;
import com.jindi.infra.dataapi.spacecloud.param.SpaceCloudParam;
import com.jindi.infra.dataapi.spacecloud.properties.RomaConfigProperties;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RomaHttpClient extends AbstractGraphQLHttpClient {

    private static final String ROMA_SERVICES_HUAWEI_DOMAIN_SWITCH = "roma.services.huawei.domain.switch";
    private static final String DEFAULT = "default";
    @Autowired
    private RomaConfigProperties romaConfigProperties;
    @Autowired
    private Environment environment;
    private RestTemplate restTemplate;
    private Counter counter;
    private Gauge gauge;
    @Autowired
    private ObjectProvider<CollectorRegistry> collectorRegistryObjectProvider;
    @Value("${spring.application.name}")
    private String application;

    public RomaHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initMethod() {
        CollectorRegistry collectorRegistry = collectorRegistryObjectProvider.getIfAvailable();
        if (collectorRegistry == null) {
            return;
        }
        Counter.Builder counterBuilder = Counter
                .build()
                .namespace(SpaceCloudConsts.ROMA)
                .subsystem(SpaceCloudConsts.CLIENT)
                .name(SpaceCloudConsts.COUNT)
                .labelNames(SpaceCloudConsts.APPLICATION, SpaceCloudConsts.RESOURCE, SpaceCloudConsts.PROJECT, SpaceCloudConsts.SERVICE, SpaceCloudConsts.ENDPOINT)
                .help("roma 客户端调用计数");
        counter = counterBuilder.register(collectorRegistry);
        Gauge.Builder gaugeBuilder = Gauge.build()
                .namespace(SpaceCloudConsts.ROMA)
                .subsystem(SpaceCloudConsts.CLIENT)
                .name(SpaceCloudConsts.RESPONSE_TIME)
                .labelNames(SpaceCloudConsts.APPLICATION, SpaceCloudConsts.RESOURCE, SpaceCloudConsts.PROJECT, SpaceCloudConsts.SERVICE, SpaceCloudConsts.ENDPOINT)
                .help("roma 客户端调用耗时");
        gauge = gaugeBuilder.register(collectorRegistry);
    }

    @Override
    public <T> List<T> getList(Class<T> responseType, Object body) {
        String data = getRaw(body);
        if (data == null) {
            return Collections.emptyList();
        }
        return JSONObject.parseArray(JSONObject.parseObject(data).getString(DEFAULT), responseType);
    }

    @Override
    public <T> T getOne(Class<T> responseType, Object body) {
        List<T> list = getList(responseType, body);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> T get(Class<T> responseType, Object body) {
        String data = getRaw(body);
        if (data == null) {
            return null;
        }
        return InnerJSONUtils.parseObject(data, responseType);
    }

    @Override
    public String getRaw(Object body) {
        SpaceCloudParam spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
        if (spaceCloudParam == null) {
            throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
        }
        logEvent("getRaw", body);
        return query(spaceCloudParam.getProject(), spaceCloudParam.getService(), spaceCloudParam.getEndpoint(), InnerJSONUtils.toJSONString(body));
    }

    public String query(String project, String service, String endpoint, String body) {
        String urlPrefix = matchUrlPrefix(project, service, endpoint);
        String url = urlPrefix + "/" + project + "/services/" + service + "/" + endpoint;
        Gauge.Timer timer = null;
        try (Entry ignored = SphU.entry(url)) {
            if (gauge != null) {
                timer = gauge.labels(application, urlPrefix, project, service, endpoint).startTimer();
            }
            log.debug("roma url: {}", url);
            if (counter != null) {
                counter.labels(application, urlPrefix, project, service, endpoint).inc();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
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

    @Override
    public GraphQLTypeEnums getGraphQLTypeEnums() {
        return GraphQLTypeEnums.ROMA;
    }

    @Override
    public String matchUrlPrefix(String project, String service, String endpoint) {
        if (!multiDomainSwitch()) {
            return romaConfigProperties.getSingleDomain();
        }
        if (StringUtils.contains(romaConfigProperties.getProjects(), project)) {
            return String.format("http://%s-%s", project.replace("_", "-"), romaConfigProperties.getMultiDomain());
        }
        return romaConfigProperties.getSingleDomain();
    }

    private Boolean multiDomainSwitch() {
        String open = environment.getProperty(ROMA_SERVICES_HUAWEI_DOMAIN_SWITCH);
        if (StringUtils.isBlank(open)) {
            return romaConfigProperties.getMultiDomainSwitch();
        }
        return Boolean.parseBoolean(open);
    }
}
