package com.jindi.infra.trace.decorator;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.jindi.infra.trace.utils.TraceMDCUtil;

import io.opentracing.Span;
import io.opentracing.contrib.kafka.SpanDecorator;

public class KafkaLoggerTraceSpanDecorator implements SpanDecorator {

	@Override
	public <K, V> void onSend(ProducerRecord<K, V> producerRecord, Span span) {
	}

	@Override
	public <K, V> void onResponse(ConsumerRecord<K, V> consumerRecord, Span span) {
		TraceMDCUtil.putTraceInfo(span.context());
	}

	@Override
	public <K, V> void onError(Exception e, Span span) {

	}
}
