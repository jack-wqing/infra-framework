package com.jindi.infra.cache.redis.exception;


public class TycRedisException extends RuntimeException{

    public TycRedisException() {
    }

    public TycRedisException(String message) {
        super(message);
    }

    public TycRedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public TycRedisException(Throwable cause) {
        super(cause);
    }

    public TycRedisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
