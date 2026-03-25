package com.hms.doctor.exception;

import lombok.Getter;

@Getter
public class DoctorScheduleNotFoundException extends RuntimeException {
    private final String id;

    public DoctorScheduleNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public DoctorScheduleNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
