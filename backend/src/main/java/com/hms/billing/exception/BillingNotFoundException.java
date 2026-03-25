package com.hms.billing.exception;

import lombok.Getter;

@Getter
public class BillingNotFoundException extends RuntimeException {
    private final String id;

    public BillingNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    public BillingNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
