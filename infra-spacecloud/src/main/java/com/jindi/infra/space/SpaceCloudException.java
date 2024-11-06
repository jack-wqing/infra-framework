package com.jindi.infra.space;

public class SpaceCloudException extends RuntimeException {

    public SpaceCloudException() {
        super();
    }

    public SpaceCloudException(String message) {
        super(message);
    }

    public SpaceCloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpaceCloudException(Throwable cause) {
        super(cause);
    }

    protected SpaceCloudException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
