package com.jindi.infra.datasource.exception;


public class TycDataSourceException extends RuntimeException{

    public TycDataSourceException() {
        super();
    }

    public TycDataSourceException(String message) {
        super(message);
    }

    public TycDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TycDataSourceException(Throwable cause) {
        super(cause);
    }

    protected TycDataSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
