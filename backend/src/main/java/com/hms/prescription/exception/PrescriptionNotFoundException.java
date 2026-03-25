package com.hms.prescription.exception;

import lombok.Getter;

@Getter
public class PrescriptionNotFoundException extends RuntimeException {
    private final String id;

    public PrescriptionNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public PrescriptionNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
