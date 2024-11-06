package com.jindi.infra.trace;

import com.jindi.infra.cache.redis.interceptor.RedisInterceptor;
import com.jindi.infra.dataapi.oneservice.annotation.OneService;
import com.jindi.infra.dataapi.oneservice.annotation.OneServiceApi;
import com.jindi.infra.datasource.metrics.DataSourceMetricsBinder;
import com.jindi.infra.trace.cache.RedisTracingInterceptor;
import com.jindi.infra.trace.grpc.filter.TraceContextServerInterceptor;
import com.jindi.infra.trace.grpc.filter.TraceGrpcCoreServerInterceptor;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.mysql.SqlTracingInterceptorDefaultConfig;
import com.jindi.infra.trace.mysql.SqlTracingInterceptorWithJdbcConfig;
import com.jindi.infra.trace.oneservice.OneServiceTracingAspect;
import com.jindi.infra.trace.reporter.FileSpanReporter;
import com.jindi.infra.trace.sampler.BoundarySampler;
import com.jindi.infra.trace.sampler.Sampler;
import com.jindi.infra.trace.utils.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TraceAutoConfiguration.RedisTracingAutoConfiguration.class,
        TraceAutoConfiguration.SqlTracingAutoConfiguration.class,
        TraceAutoConfiguration.OneServiceTracingAutoConfiguration.class
})
public class TraceAutoConfiguration {

    @Value("${trace.sample.probability:0.001}")
    private float probability;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${logger.path.name:}")
    private String loggerName;

    @ConditionalOnMissingBean
    @Bean
    public TraceContext traceContext() {
        return new TraceContext();
    }

    @ConditionalOnMissingBean
    @Bean
    public FileSpanReporter spanFileReporter() {
        String loggerPath = applicationName;
        if(StringUtils.isNotBlank(loggerName)) {
            loggerPath = loggerName;
        }
        return new FileSpanReporter(loggerPath);
    }

    @ConditionalOnMissingBean
    @Bean
    public Sampler getBoundarySampler() {
        return BoundarySampler.create(probability);
    }

    @ConditionalOnMissingBean
    @Bean
    public SpringBeanUtils springBeanUtils() {
        return new SpringBeanUtils();
    }

    @ConditionalOnMissingBean(name = "traceGrpcCoreServerInterceptor")
    @Bean(name = "traceGrpcCoreServerInterceptor")
    public TraceGrpcCoreServerInterceptor traceGrpcCoreServerInterceptor() {
        return new TraceGrpcCoreServerInterceptor();
    }

    @ConditionalOnMissingBean
    @Bean
    public TraceContextServerInterceptor traceContextServerInterceptor () {
        return new TraceContextServerInterceptor();
    }

    @Configuration
    @ConditionalOnClass(name = "org.apache.ibatis.plugin.Interceptor")
    public class SqlTracingAutoConfiguration {

        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "com.jindi.infra.datasource.metrics.DataSourceMetricsBinder")
        @Bean
        public SqlTracingInterceptorWithJdbcConfig sqlTracingInterceptorWithJdbcConfig () {
            return new SqlTracingInterceptorWithJdbcConfig();
        }

        @ConditionalOnMissingBean
        @ConditionalOnMissingClass("com.jindi.infra.datasource.metrics.DataSourceMetricsBinder")
        @Bean
        public SqlTracingInterceptorDefaultConfig sqlTracingInterceptorDefaultConfig () {
            return new SqlTracingInterceptorDefaultConfig();
        }
    }

    @Configuration
    @ConditionalOnClass({RedisInterceptor.class})
    public class RedisTracingAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        public RedisTracingInterceptor tracingInterceptor (TraceContext traceContext) {
            return new RedisTracingInterceptor(traceContext);
        }
    }

    @Configuration
    @ConditionalOnClass({OneServiceApi.class, OneService.class})
    public class OneServiceTracingAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        public OneServiceTracingAspect oneServiceTracingAspect () {
            return new OneServiceTracingAspect();
        }
    }

}
