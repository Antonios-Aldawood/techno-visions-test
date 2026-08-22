package com.technovisions.orderaggregator.exception;

/** Raised when the system service returns 404 for a lookup the aggregator forwarded. */
public class DownstreamNotFoundException extends RuntimeException {

    public DownstreamNotFoundException(String message) {
        super(message);
    }
}
