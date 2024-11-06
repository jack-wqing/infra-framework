package com.jindi.infra.mq.kafka.serializer;

import static com.jindi.infra.mq.kafka.constant.KafkaConsts.PROTOBUF_TYPE;

import java.lang.reflect.Method;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.jindi.infra.mq.kafka.exception.KafkaException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtobufDeserializer implements Deserializer<Message> {

    @Override
    public Message deserialize(String topic, Headers headers, byte[] data) {
        Header header = headers.lastHeader(PROTOBUF_TYPE);
        if (header == null) {
            throw new KafkaException("未定义ProtoSerializer header: protoType");
        }
        String protoType = new String(header.value());
        try {
            Class<?> message = Class.forName(protoType);
            Method newBuilder = message.getDeclaredMethod("newBuilder");
            Message.Builder builder = (Message.Builder) newBuilder.invoke(message, null);
            JsonFormat.merge(new String(data), builder);
            return builder.build();
        } catch (Exception e) {
            throw new KafkaException("反序列化proto对象失败", e);
        }
    }

    @Override
    public Message deserialize(String topic, byte[] data) {
        throw new KafkaException("执行错误 deserialize(topic, data)");
    }
}
