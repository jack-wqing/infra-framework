# infra-metrics

> 对服务指标进行度量和监控，目前已经集成Prometheus和Cat

**特点**

1. 默认支持servlet, grpc, dubbo 等网络io组件集成，开箱即用
2. 基于注解，自定义打点
3. 灵活的api设计，方便封装成其他高级组件

## 接入方式

* 依赖jar

```xml

<dependency>
    <groupId>com.jindi.infra</groupId>
    <artifactId>infra-metrics</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

**配置项**

1. spring-boot配置文件

```properties
management.endpoints.web.exposure.include=*
```

2. 独有的配置文件 (在resources目录下创建 META-INF/app.properties文件)

```properties
app.name={项目名}
```

**自定义打点**

* 基于注解的自定义事务

```java
import com.jindi.infra.metrics.cat.aop.CatTransaction;
import com.jindi.infra.metrics.cat.constant.CatType;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class Sample {

    /**
     * 默认: type等于Call, name等于方法名 eg: Sample.login 同时会对Sample.login计数器进行加一操作
     */
    @CatTransaction
    public Boolean login(String name) {
        if (Objects.equals("2021发财", name)) {
            return true;
        }
        throw new RuntimeException("不可能运行到这里");
    }

    /**
     * 自定义type和name
     * @return
     */
    @CatTransaction(type = CatType.CALL, name = "注销", metric = 1)
    public Boolean logout() {
        return true;
    }
}
```

**扩展能力**

* 支持数据库CRUD的监控

```java

@Configuration
public class DatabaseConfig {

    @Bean(sqlSessionFactory)
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Throwable {
        DataSource dataSource = dataSources.get(INSTANCE);
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.getObject().getConfiguration().setCallSettersOnNulls(true);
        /**
         * 配置mybatis的拦截器, 对数据库访问进行监控
         */
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{new CatMybatisInterceptor("jdbc:mysql://localhost:3306/prism1")});
        return sqlSessionFactoryBean.getObject();
    }
}

```
