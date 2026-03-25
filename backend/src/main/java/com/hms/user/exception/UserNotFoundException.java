package com.hms.user.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final String id;

    public UserNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public UserNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
