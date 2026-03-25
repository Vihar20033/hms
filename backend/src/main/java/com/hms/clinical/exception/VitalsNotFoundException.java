package com.hms.clinical.exception;

import lombok.Getter;

@Getter
public class VitalsNotFoundException extends RuntimeException {
    private final String id;

    public VitalsNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public VitalsNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
