package com.jindi.infra.dataapi.spacecloud;

public class OneServiceException extends RuntimeException {

    public OneServiceException() {
        super();
    }

    public OneServiceException(String message) {
        super(message);
    }

    public OneServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public OneServiceException(Throwable cause) {
        super(cause);
    }

    protected OneServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
