package com.jindi.infra.trace.decorator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * trace span decorator autoConfiguration 生成span后，设置traceId到MDC中
 */
@Configuration
@ConditionalOnProperty(value = "trace.span.decorator.enable", matchIfMissing = true)
public class TraceSpanDecoratorAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "feign.Request")
	public FeignLoggerTraceSpanDecorator feignLoggerTraceSpanDecorator() {
		return new FeignLoggerTraceSpanDecorator();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "javax.servlet.http.HttpServletRequest")
	public HttpLoggerTraceSpanDecorator httpLoggerTraceSpanDecorator() {
		return new HttpLoggerTraceSpanDecorator();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "org.apache.kafka.clients.producer.ProducerRecord")
	public KafkaLoggerTraceSpanDecorator kafkaLoggerTraceSpanDecorator() {
		return new KafkaLoggerTraceSpanDecorator();
	}

}
