package com.hms.common.exception;

/**
 * Exception thrown when a request is invalid (bad input, missing required fields, etc.).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
