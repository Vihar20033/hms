package com.hms.auth.exception;

public class SelfRegistrationRoleNotAllowedException extends RuntimeException {

    public SelfRegistrationRoleNotAllowedException(String message) {
        super(message);
    }
}
