package com.hms.appointment.exception;

public class DoctorUnavailableException extends RuntimeException {

    public DoctorUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
