package com.hms.appointment.exception;

public class DuplicateAppointmentException extends RuntimeException {

    public DuplicateAppointmentException(String message) {
        super(message);
    }

    public DuplicateAppointmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
