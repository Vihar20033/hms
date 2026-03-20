package com.hms.auth;

public class SelfRegistrationRoleNotAllowedException extends RuntimeException {

    public SelfRegistrationRoleNotAllowedException(String message) {
        super(message);
    }
}
