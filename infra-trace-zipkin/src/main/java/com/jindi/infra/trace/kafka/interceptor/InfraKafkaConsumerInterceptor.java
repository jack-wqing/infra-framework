package com.jindi.infra.trace.kafka.interceptor;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.trace.kafka.context.KafkaTraceContext;
import com.jindi.infra.trace.model.TraceContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfraKafkaConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

    private static TraceContext traceContext;

    @Override
    public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
        if (records == null) {
            return null;
        }

        try {
            KafkaTraceContext kafkaTraceContext = new KafkaTraceContext(getTraceContext());
            if (records.count() == 1) {
                // 只拉取到1条记录，透传trace
                ConsumerRecord<K, V> record = records.iterator().next();
                if (record != null) {
                    kafkaTraceContext.buildConsumerTracePropagation(record.headers());
                }
            } else {
                // 大于1条记录，不透传之前的traceId，创建新的trace
                kafkaTraceContext.buildNewConsumerTracePropagation();
            }
        } catch (Exception e) {
            log.error("onConsume error, records.count: {}", records.count(), e);
        }
        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }


    private static TraceContext getTraceContext() {
        if (traceContext != null) {
            return traceContext;
        }
        traceContext = ACUtils.getBean(TraceContext.class);
        return traceContext;
    }
}
