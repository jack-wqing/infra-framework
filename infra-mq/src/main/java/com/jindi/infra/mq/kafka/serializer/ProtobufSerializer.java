package com.jindi.infra.mq.kafka.serializer;


import static com.jindi.infra.mq.kafka.constant.KafkaConsts.PROTOBUF_TYPE;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.jindi.infra.mq.kafka.exception.KafkaException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtobufSerializer implements Serializer<Message> {

    @Override
    public byte[] serialize(String topic, Headers headers, Message data) {
        headers.add(PROTOBUF_TYPE, data.getClass().getName().getBytes(StandardCharsets.UTF_8));
        String s = JsonFormat.printToString(data);
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] serialize(String topic, Message data) {
        throw new KafkaException("执行错误的 serialize(topic, data)");
    }
}
