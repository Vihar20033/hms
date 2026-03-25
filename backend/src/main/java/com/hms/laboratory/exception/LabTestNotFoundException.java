package com.hms.laboratory.exception;

import lombok.Getter;

@Getter
public class LabTestNotFoundException extends RuntimeException {
    private final String id;

    public LabTestNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public LabTestNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
