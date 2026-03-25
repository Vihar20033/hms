package com.hms.doctor.exception;

import lombok.Getter;
import java.util.UUID;

@Getter
public class DoctorNotFoundException extends RuntimeException {
    private final String id;

    public DoctorNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public DoctorNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }

    public DoctorNotFoundException(UUID id) {
        super("Doctor profile not found with ID: " + id);
        this.id = id.toString();
    }
}
