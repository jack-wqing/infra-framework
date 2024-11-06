package com.jindi.infra.mq.kafka.interceptor;

import com.dianping.cat.Cat;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

@Slf4j
public class CatConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

	private static final String MQ_KAFKA_CONSUMER_ON_COMMIT = "mq.kafka.consumer.onCommit";

	@Override
	public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
		return records;
	}

	@Override
	public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
		try {
			for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
				TopicPartition topicPartition = entry.getKey();
				Cat.logEvent(MQ_KAFKA_CONSUMER_ON_COMMIT, topicPartition.topic());
			}
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
