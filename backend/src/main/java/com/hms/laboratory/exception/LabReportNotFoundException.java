package com.hms.laboratory.exception;

import lombok.Getter;

@Getter
public class LabReportNotFoundException extends RuntimeException {
    private final String id;

    public LabReportNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public LabReportNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
