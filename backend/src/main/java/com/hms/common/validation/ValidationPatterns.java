package com.hms.common.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final String USERNAME = "^[A-Za-z0-9](?:[A-Za-z0-9._-]{1,48}[A-Za-z0-9])?$";
    public static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{8,128}$";
    public static final String PERSON_NAME = "^[A-Za-z][A-Za-z .'-]{0,49}$";
    public static final String FULL_NAME = "^[A-Za-z][A-Za-z .'-]{1,199}$";
    public static final String PHONE = "^\\+?[0-9]{10,15}$";
    public static final String LICENSE_NUMBER = "^[A-Za-z0-9/-]{4,50}$";
    public static final String CODE = "^[A-Za-z0-9][A-Za-z0-9._/-]{1,49}$";
}
