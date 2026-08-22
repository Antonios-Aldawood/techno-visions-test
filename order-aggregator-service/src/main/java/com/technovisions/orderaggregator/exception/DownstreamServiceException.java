package com.technovisions.orderaggregator.exception;

/** Raised when the system service is unreachable or returns an unexpected error status. */
public class DownstreamServiceException extends RuntimeException {

    public DownstreamServiceException(String message) {
        super(message);
    }

    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
