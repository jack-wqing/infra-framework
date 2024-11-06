package com.jindi.infra.mq.kafka.interceptor;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.dianping.cat.Cat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

	private static final String MQ_KAFKA_SEND = "mq.kafka.send";
	private static final String MQ_KAFKA_ON_ACKNOWLEDGEMENT = "mq.kafka.onAcknowledgement";

	@Override
	public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
		try {
			if (record == null) {
				return null;
			}
			Cat.logEvent(MQ_KAFKA_SEND, record.topic());
		} catch (Throwable e) {
			log.debug("", e);
		}
		return record;
	}

	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
		if (exception == null) {
			return;
		}
		try {
			log.error("kafka send failure metadata = {}", metadata, exception);
			Cat.logError(MQ_KAFKA_ON_ACKNOWLEDGEMENT, exception);
		} catch (Throwable e) {
			log.debug("", e);
		}
	}

	@Override
	public void close() {
	}

	@Override
	public void configure(Map<String, ?> configs) {
	}
}
