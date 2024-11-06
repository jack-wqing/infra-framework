package com.jindi.infra.mq.kafka.exception;

public class KafkaException extends RuntimeException {

    public KafkaException() {
        super();
    }

    public KafkaException(String message) {
        super(message);
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
