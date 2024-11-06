## spring sleuth 源代码分析

**spring-cloud-starter-sleuth, spring-cloud-starter-zipkin**

* 依赖

```
+- org.springframework.cloud:spring-cloud-starter-sleuth:jar:3.0.0:compile
|  +- org.springframework.cloud:spring-cloud-starter:jar:3.0.0:compile
|  |  +- org.springframework.cloud:spring-cloud-context:jar:3.0.0:compile
|  |  \- org.springframework.security:spring-security-rsa:jar:1.0.9.RELEASE:compile
|  |     \- org.bouncycastle:bcpkix-jdk15on:jar:1.64:compile
|  |        \- org.bouncycastle:bcprov-jdk15on:jar:1.64:compile
|  +- org.springframework.boot:spring-boot-starter-aop:jar:2.4.1:compile
|  |  +- org.springframework:spring-aop:jar:5.3.2:compile
|  |  \- org.aspectj:aspectjweaver:jar:1.9.6:compile
|  +- org.springframework.cloud:spring-cloud-sleuth-autoconfigure:jar:3.0.0:compile
|  |  \- org.aspectj:aspectjrt:jar:1.9.6:compile
|  \- org.springframework.cloud:spring-cloud-sleuth-brave:jar:3.0.0:compile
|     +- io.zipkin.brave:brave-context-slf4j:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-messaging:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-spring-rabbit:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-kafka-clients:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-kafka-streams:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-httpclient:jar:5.13.2:compile
|     |  \- io.zipkin.brave:brave-instrumentation-http:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-httpasyncclient:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-jms:jar:5.13.2:compile
|     +- io.zipkin.brave:brave-instrumentation-mongodb:jar:5.13.2:compile
|     +- io.zipkin.aws:brave-propagation-aws:jar:0.21.3:compile
|     \- io.zipkin.reporter2:zipkin-reporter-metrics-micrometer:jar:2.16.1:compile
+- org.springframework.cloud:spring-cloud-sleuth-zipkin:jar:3.0.0:compile
|  +- org.springframework.cloud:spring-cloud-sleuth-instrumentation:jar:3.0.0:compile
|  |  \- org.springframework.cloud:spring-cloud-sleuth-api:jar:3.0.0:compile
|  +- org.springframework:spring-web:jar:5.3.2:compile
|  |  \- org.springframework:spring-beans:jar:5.3.2:compile
|  +- org.springframework.cloud:spring-cloud-commons:jar:3.0.0:compile
|  |  \- org.springframework.security:spring-security-crypto:jar:5.4.2:compile
|  +- io.zipkin.zipkin2:zipkin:jar:2.23.0:compile
|  +- io.zipkin.reporter2:zipkin-reporter:jar:2.16.1:compile
|  +- io.zipkin.reporter2:zipkin-reporter-brave:jar:2.16.1:compile
|  +- io.zipkin.reporter2:zipkin-sender-kafka:jar:2.16.1:compile
|  +- io.zipkin.reporter2:zipkin-sender-activemq-client:jar:2.16.1:compile
|  \- io.zipkin.reporter2:zipkin-sender-amqp-client:jar:2.16.1:compile
```

* 自动配置类 位于spring-cloud-sleuth-autoconfigure

```
# Auto Configuration
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.sleuth.autoconfig.instrument.async.TraceAsyncAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.async.TraceAsyncCustomAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.async.TraceAsyncDefaultAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.circuitbreaker.TraceCircuitBreakerAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.rxjava.TraceRxJavaAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.quartz.TraceQuartzAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.web.client.TraceWebClientAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.web.client.feign.TraceFeignClientAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.web.client.TraceWebAsyncClientAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.scheduling.TraceSchedulingAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.reactor.TraceReactorAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.messaging.TraceFunctionAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.messaging.TraceSpringIntegrationAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.messaging.TraceSpringMessagingAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.instrument.messaging.TraceWebSocketAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.web.client.BraveWebClientAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.rpc.BraveRpcAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.grpc.BraveGrpcAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.messaging.BraveKafkaStreamsAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.messaging.BraveMessagingAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.opentracing.BraveOpentracingAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.redis.BraveRedisAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.brave.instrument.mongodb.BraveMongoDbAutoConfiguration,\
org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinAutoConfiguration
# Environment Post Processor
org.springframework.boot.env.EnvironmentPostProcessor=\
org.springframework.cloud.sleuth.autoconfig.TraceEnvironmentPostProcessor,\
org.springframework.cloud.sleuth.autoconfig.instrument.web.client.TraceGatewayEnvironmentPostProcessor
```

* 核心配置类, 其他的大部分都是对某种技术组件的集成

```
org.springframework.cloud.sleuth.autoconfig.brave.BraveSamplerConfiguration 创建 brave.sampler.Sampler 
org.springframework.cloud.sleuth.autoconfig.brave.instrument.web.BraveHttpConfiguration 创建 brave.http.HttpTracing
org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration 创建 brave.Tracing，brave.Tracer, brave.CurrentSpanCustomizer
org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinBraveConfiguration 创建 brave.TracingCustomizer, brave.handler.SpanHandler
org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinAutoConfiguration 创建 zipkin2.reporter.Reporter, zipkin2.reporter.Sender
```

* [OpenTracing语义标准](https://opentracing-contrib.github.io/opentracing-specification-zh/specification.html)
