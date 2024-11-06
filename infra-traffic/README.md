# infra-traffic

> 目前主要是基于sentinel，实现以流量为切入点，从流量控制、熔断降级、系统负载保护等多个维度保护服务的稳定性。

**特点**

1. 添加Grpc的adapter
2.

拥抱 [https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

## 接入方式

* 依赖jar

```xml

<dependency>
    <groupId>com.jindi.infra</groupId>
    <artifactId>infra-traffic</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

* 配置

```yaml
spring:
  cloud:
    sentinel:
      datasource:
        ds1:
          nacos:
            data-id: infra-grpc-sample-api-flow-rules
            data-type: json
            group-id: infra-grpc-sample-api
            namespace: sentinel
            password: nacos
            rule-type: flow
            server-addr: localhost:8848
            username: nacos
      transport:
        dashboard: 172.26.123.195:9090
        port: 8719
```
