package com.jindi.infra.core.exception;

public class IllegalRegionException extends RuntimeException {

    public IllegalRegionException() {
        super();
    }

    public IllegalRegionException(String message) {
        super(message);
    }

    public IllegalRegionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalRegionException(Throwable cause) {
        super(cause);
    }

    protected IllegalRegionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
