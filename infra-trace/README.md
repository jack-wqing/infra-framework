# infra-trace

* 配置

```yaml
trace:
    zipkin:
        samplerProbability: 0.01
        queuedMaxSpans: 10000
    kafka:
        servers: localhost:9092
        topic: zipkin
```

**资源**

* [https://docs.spring.io/spring-cloud-sleuth/docs/current/reference/html/index.html](https://docs.spring.io/spring-cloud-sleuth/docs/current/reference/html/index.html)
