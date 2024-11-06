package com.jindi.infra.trace.kafka.interceptor;

import com.jindi.infra.core.util.ACUtils;
import com.jindi.infra.trace.consts.TagsConsts;
import com.jindi.infra.trace.kafka.context.KafkaTraceContext;
import com.jindi.infra.trace.model.Span;
import com.jindi.infra.trace.model.TraceContext;
import com.jindi.infra.trace.propagation.TracePropagation;
import com.jindi.infra.trace.utils.TraceMDCUtil;
import com.jindi.infra.trace.utils.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.Objects;

/**
 * 拦截器生成修改：优化trace span的保存时机
 * 因为TraceContext未能实现Scope功能，这个使用ThreadLocal实现
 * @param <K>
 * @param <V>
 */

@Slf4j
public class InfraKafkaProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

    private static TraceContext traceContext;

    private static final ThreadLocal<Span> spanThreadLocal = new ThreadLocal<>();

    @Override
    public ProducerRecord onSend(ProducerRecord<K, V> record) {
        if (record == null) {
            return null;
        }
        try {
            //从MDC获取trace信息 写入record的headers中
            KafkaTraceContext kafkaTraceContext = new KafkaTraceContext(getTraceContext());
            TracePropagation tracePropagation = TraceMDCUtil.getCurrentTracePropagation();
            kafkaTraceContext.buildProducerTracePropagation(tracePropagation, record.headers());
            Span span = createSpan(tracePropagation, record.topic());
            spanThreadLocal.set(span);
        } catch (Throwable e) {
            log.error("onSend error", e);
        }
        return record;
    }

    private Span createSpan(TracePropagation tracePropagation, String topic) {
        return traceContext.buildSpan(tracePropagation, "producer-" + topic, Span.KindEnum.PRODUCER, topic);
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        try {
            Span span = spanThreadLocal.get();
            if (Objects.nonNull(span)) {
                if (Objects.nonNull(exception)) {
                    TraceUtil.tag(span, TagsConsts.ERROR, exception.getMessage());
                }
                traceContext.writeSpan(span);
            }
        } catch (Exception e) {
            log.error("send kafka error", e);
        } finally {
            spanThreadLocal.remove();
        }
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
