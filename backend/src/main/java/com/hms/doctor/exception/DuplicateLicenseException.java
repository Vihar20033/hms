package com.hms.doctor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a doctor with a duplicate license number
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLicenseException extends RuntimeException {

    public DuplicateLicenseException(String message) {
        super(message);
    }

    public DuplicateLicenseException(String licenseNumber, String message) {
        super("License number '" + licenseNumber + "' " + message);
    }
}
