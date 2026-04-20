package com.hms.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HmsErrorCode {
    
    // General Errors
    INTERNAL_SERVER_ERROR("ERR_INTERNAL", "An unexpected error occurred"),
    VALIDATION_FAILED("ERR_VALIDATION", "Entity validation failed"),
    ACCESS_DENIED("ERR_FORBIDDEN", "Access denied for this resource"),
    UNAUTHORIZED("ERR_UNAUTHORIZED", "Authentication required"),
    
    // Authentication Errors
    INVALID_CREDENTIALS("AUTH_001", "Invalid username or password"),
    USERNAME_EXISTS("AUTH_002", "Username already taken"),
    TOKEN_EXPIRED("AUTH_003", "Session has expired"),
    EMAIL_ALREADY_EXISTS("AUTH_004", "Email address already in use"),
    
    // Patient Errors
    PATIENT_NOT_FOUND("PAT_001", "Patient not found"),
    DUPLICATE_PATIENT("PAT_002", "Patient already exists"),
    
    // Appointment Errors
    APPOINTMENT_NOT_FOUND("APP_001", "Appointment not found"),
    SLOT_OCCUPIED("APP_002", "Selected time slot is already booked"),
    DOCTOR_UNAVAILABLE("APP_003", "Doctor is not available at the requested time"),

    // Doctor Errors
    DOCTOR_NOT_FOUND("DOC_001", "Doctor record not found"),

    // Request Errors
    BAD_REQUEST("REQ_001", "The request is invalid"),
    RATE_LIMIT_EXCEEDED("REQ_002", "Too many requests"),
    REQUEST_CONFLICT("REQ_003", "The request conflicts with current resource state"),
    
    // User Errors
    USER_NOT_FOUND("AUTH_006", "User record not found"),
    
    // Pharmacy Errors
    MEDICINE_NOT_FOUND("PHR_001", "Medicine not found in inventory"),
    INSUFFICIENT_STOCK("PHR_002", "Insufficient medicine quantity in stock"),
    DUPLICATE_MEDICINE("PHR_003", "Medicine code already exists"),


    // Billing Errors
    BILLING_NOT_FOUND("BIL_001", "Billing record not found"),

    // Prescription Errors
    PRESCRIPTION_NOT_FOUND("PRE_001", "Prescription not found"),

    // Workflow Errors
    WORKFLOW_DEFINITION_NOT_FOUND("WFL_001", "Workflow definition not found"),
    WORKFLOW_INSTANCE_NOT_FOUND("WFL_002", "Workflow instance not found"),
    WORKFLOW_STATE_INVALID("WFL_003", "Workflow is not in a valid state for this operation"),
    WORKFLOW_VALIDATION_FAILED("WFL_004", "Workflow request validation failed"),

    // Audit Errors
    AUDIT_ENTITY_TYPE_NOT_SUPPORTED("AUD_001", "Requested audit entity type is not supported");

    private final String code;
    private final String defaultMessage;
}
