package com.hms.audit.exception;

public class AuditEntityTypeNotSupportedException extends RuntimeException {

    public AuditEntityTypeNotSupportedException(String message) {
        super(message);
    }
}
