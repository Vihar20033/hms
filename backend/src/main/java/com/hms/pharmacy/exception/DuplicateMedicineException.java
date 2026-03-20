package com.hms.pharmacy.exception;

public class DuplicateMedicineException extends RuntimeException {
    public DuplicateMedicineException(String message) {
        super(message);
    }
}
