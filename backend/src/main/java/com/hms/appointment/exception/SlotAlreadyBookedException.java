package com.hms.appointment.exception;

/**
 * Exception thrown when attempting to book an appointment slot that is already taken.
 * This can happen due to race conditions even after checking availability.
 */
public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException(String message) {
        super(message);
    }

    public SlotAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }
}
