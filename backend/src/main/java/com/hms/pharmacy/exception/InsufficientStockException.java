package com.hms.pharmacy.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final String medicineName;

    public InsufficientStockException(String medicineName, String message) {
        super(message);
        this.medicineName = medicineName;
    }
}
