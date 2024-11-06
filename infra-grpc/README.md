# infra-grpc入门

> 对RPC进行重新定义；基于Grpc业内神级框架进行二开，实现了 服务治理，并让使用更加丝滑，增加了回退机制，方法级超时等功能。支持和内部系统进行深度集成

**依赖jar**

```xml

<dependency>
    <groupId>com.jindi.infra</groupId>
    <artifactId>infra-framework-starter</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

**服务端**

* SDK包中定义protobuf文件

```protobuf
syntax = "proto3";

package demo;

option java_package = "com.jindi.demo";

service GreeterService {

  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

message HelloRequest {

  string name = 1;
}

message HelloReply {

  string message = 1;
}
```

* 本地安装SDK，并生成Stub文件

```text
mvn clean install
```

* 实现服务

```java

@RPCService
public class GreeterServiceImpl extends GreeterService {

    @Override
    public Hello.HelloReply sayHello(Hello.HelloRequest request) {
        return Hello.HelloReply.newBuilder()
                .setMessage(String.format("hi %s", request.getName()))
                .build();
    }
}
```

* 启动类

```java

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Application.class, args);
        System.in.read();
    }
}
```

**客户端**

* 依赖SDK包，并引入服务

```java

@Service
public class GreeterServiceProxy {

    @RPCCall private GreeterService greeterService;

    public String hello(String name) {
        Hello.HelloReply helloReply =
                greeterService.sayHello(Hello.HelloRequest.newBuilder().setName(name).build());
        return helloReply.getMessage();
    }
}
```

* 实现回退逻辑

```java

@Slf4j
@RPCFallback
public class GreeterServiceFallback extends GreeterService {

    @RPCCallOption(exception = Throwable.class, retryCount = 1)
    @Override
    public Hello.HelloReply sayHello(Hello.HelloRequest request) {
        return Hello.HelloReply.newBuilder()
                .setMessage(String.format("greeter hello name = %s fallback", request.getName()))
                .build();
    }
}

```

* 启动类

```java

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 配置项

> 并不是每个配置项目都需要设置； 一般情况下，仅需要服务端定义端口，防止混部署时的情况下出现端口冲突；默认端口为 9999（寓意4个9）

```yaml
rpc:
  enable: true # rpc功能开关
  server:
    port: 9999 # 服务端监听端口
    awaitTerminationSecond: 5 # 等待5秒终止
  client:
    direct: true # 直连
    target: localhost:9999 # 客户端直连
    connectTimeoutMillis: 100 # 连接超时时长
    callTimeoutMillis: 1000 # 调用超时
    services:
      - name: com.jindi.infra.demo.sdk.GreeterService
        callTimeoutMillis: 500
        methods:
          - name: sayHello
            callTimeoutMillis: 50
```