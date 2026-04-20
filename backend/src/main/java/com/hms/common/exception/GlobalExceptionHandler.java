package com.hms.common.exception;

import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.exception.DoctorUnavailableException;
import com.hms.appointment.exception.SlotAlreadyBookedException;
import com.hms.audit.exception.AuditEntityTypeNotSupportedException;
import com.hms.billing.exception.BillingNotFoundException;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.prescription.exception.PrescriptionNotFoundException;
import com.hms.user.exception.UserNotFoundException;
import com.hms.workflow.exception.WorkflowDefinitionNotFoundException;
import com.hms.workflow.exception.WorkflowInstanceNotFoundException;
import com.hms.workflow.exception.WorkflowStateException;
import com.hms.workflow.exception.WorkflowValidationException;

import com.hms.common.response.ApiError;
import com.hms.common.response.ValidationError;
import com.hms.patient.exception.DuplicatePatientException;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.pharmacy.exception.DuplicateMedicineException;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.user.exception.EmailAlreadyExistsException;
import com.hms.user.exception.InvalidCredentialsException;
import com.hms.user.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

// Handle Exception Globally and return Json format respons
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // ========== Standard Response Build Helper ==========
        private ResponseEntity<ApiError> buildResponse(HmsErrorCode errorCode, HttpStatus status, String customMessage, HttpServletRequest request) {
            ApiError response = ApiError.of(
                    customMessage != null ? customMessage : errorCode.getDefaultMessage(),
                    errorCode.getCode(),
                    status
            );
            response.setPath(request.getRequestURI());
            return ResponseEntity.status(status).body(response);
        }

        private ResponseEntity<ApiError> buildValidationResponse(List<ValidationError> errors, HttpServletRequest request) {
                ApiError response = ApiError.of("Validation failed", HmsErrorCode.VALIDATION_FAILED.getCode(), HttpStatus.BAD_REQUEST);
                response.setValidationErrors(errors);
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // ========== Patient Exceptions ==========

        @ExceptionHandler(PatientNotFoundException.class)
        public ResponseEntity<ApiError> handlePatientNotFound(PatientNotFoundException ex, HttpServletRequest request) {
                log.warn("Patient not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.PATIENT_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(DuplicatePatientException.class)
        public ResponseEntity<ApiError> handleDuplicatePatient(DuplicatePatientException ex, HttpServletRequest request) {
                log.warn("Duplicate patient: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DUPLICATE_PATIENT, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        // ========== Appointment Exceptions ==========

        @ExceptionHandler(SlotAlreadyBookedException.class)
        public ResponseEntity<ApiError> handleSlotAlreadyBooked(SlotAlreadyBookedException ex, HttpServletRequest request) {
                log.warn("Slot already booked: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.SLOT_OCCUPIED, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(AppointmentNotFoundException.class)
        public ResponseEntity<ApiError> handleAppointmentNotFound(AppointmentNotFoundException ex, HttpServletRequest request) {
                log.warn("Appointment not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.APPOINTMENT_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(DoctorUnavailableException.class)
        public ResponseEntity<ApiError> handleDoctorUnavailable(DoctorUnavailableException ex, HttpServletRequest request) {
                log.warn("Doctor unavailable: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DOCTOR_UNAVAILABLE, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        // ========== User/Auth Exceptions ==========

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
                log.warn("Invalid credentials: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, null, request);
        }

        @ExceptionHandler(UsernameAlreadyExistsException.class)
        public ResponseEntity<ApiError> handleUsernameExists(UsernameAlreadyExistsException ex, HttpServletRequest request) {
                log.warn("Username already exists: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.USERNAME_EXISTS, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
                log.warn("Email already exists: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
                log.warn("Pharmacy stock low for {}: {}", ex.getMedicineName(), ex.getMessage());
                return buildResponse(HmsErrorCode.INSUFFICIENT_STOCK, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        @ExceptionHandler(MedicineNotFoundException.class)
        public ResponseEntity<ApiError> handleMedicineNotFound(MedicineNotFoundException ex, HttpServletRequest request) {
                log.warn("Medicine not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.MEDICINE_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(DuplicateMedicineException.class)
        public ResponseEntity<ApiError> handleDuplicateMedicine(DuplicateMedicineException ex, HttpServletRequest request) {
                log.warn("Duplicate medicine: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DUPLICATE_MEDICINE, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        // ========== Doctor Exceptions ==========

        @ExceptionHandler(DoctorNotFoundException.class)
        public ResponseEntity<ApiError> handleDoctorNotFound(DoctorNotFoundException ex, HttpServletRequest request) {
                log.warn("Doctor not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.DOCTOR_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        // ========== Billing Exceptions ==========

        @ExceptionHandler(BillingNotFoundException.class)
        public ResponseEntity<ApiError> handleBillingNotFound(BillingNotFoundException ex, HttpServletRequest request) {
                log.warn("Billing record not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BILLING_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }


        // ========== Prescription Exceptions ==========

        @ExceptionHandler(PrescriptionNotFoundException.class)
        public ResponseEntity<ApiError> handlePrescriptionNotFound(PrescriptionNotFoundException ex, HttpServletRequest request) {
                log.warn("Prescription not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.PRESCRIPTION_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        // ========== User Exceptions ==========

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
                log.warn("User not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(WorkflowDefinitionNotFoundException.class)
        public ResponseEntity<ApiError> handleWorkflowDefinitionNotFound(WorkflowDefinitionNotFoundException ex, HttpServletRequest request) {
                log.warn("Workflow definition not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.WORKFLOW_DEFINITION_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(WorkflowInstanceNotFoundException.class)
        public ResponseEntity<ApiError> handleWorkflowInstanceNotFound(WorkflowInstanceNotFoundException ex, HttpServletRequest request) {
                log.warn("Workflow instance not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.WORKFLOW_INSTANCE_NOT_FOUND, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(WorkflowValidationException.class)
        public ResponseEntity<ApiError> handleWorkflowValidation(WorkflowValidationException ex, HttpServletRequest request) {
                log.warn("Workflow validation failed: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.WORKFLOW_VALIDATION_FAILED, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        @ExceptionHandler(WorkflowStateException.class)
        public ResponseEntity<ApiError> handleWorkflowState(WorkflowStateException ex, HttpServletRequest request) {
                log.warn("Workflow state invalid: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.WORKFLOW_STATE_INVALID, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(AuditEntityTypeNotSupportedException.class)
        public ResponseEntity<ApiError> handleAuditEntityTypeNotSupported(AuditEntityTypeNotSupportedException ex, HttpServletRequest request) {
                log.warn("Audit entity type not supported: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.AUDIT_ENTITY_TYPE_NOT_SUPPORTED, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        // ========== Validation & Security ==========

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
                log.warn("Bad request: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
                log.warn("Conflict: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.REQUEST_CONFLICT, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
                List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
                        .collect(Collectors.toList());

                log.warn("Validation failed for {}: {}", request.getRequestURI(), errors);
                return buildValidationResponse(errors, request);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
                List<ValidationError> errors = ex.getConstraintViolations().stream()
                        .map(v -> new ValidationError(v.getPropertyPath().toString(), v.getMessage()))
                        .collect(Collectors.toList());
                log.warn("Constraint violation for {}: {}", request.getRequestURI(), errors);
                return buildValidationResponse(errors, request);
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ApiError> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
                List<ValidationError> errors = ex.getAllValidationResults().stream()
                        .flatMap(result -> result.getResolvableErrors().stream()
                                .map(error -> new ValidationError(result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                        .collect(Collectors.toList());
                log.warn("Handler method validation failed for {}: {}", request.getRequestURI(), errors);
                return buildValidationResponse(errors, request);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
                log.warn("Access denied: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, null, request);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
                log.warn("Authentication failed: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
        }

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
                log.warn("Entity not found: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.NOT_FOUND, ex.getMessage(), request);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
                log.warn("Illegal argument for {}: {}", request.getRequestURI(), ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
                log.warn("Illegal state for {}: {}", request.getRequestURI(), ex.getMessage());
                return buildResponse(HmsErrorCode.REQUEST_CONFLICT, HttpStatus.CONFLICT, ex.getMessage(), request);
        }

        @ExceptionHandler(SecurityException.class)
        public ResponseEntity<ApiError> handleSecurityException(SecurityException ex, HttpServletRequest request) {
                log.warn("Security exception for {}: {}", request.getRequestURI(), ex.getMessage());
                return buildResponse(HmsErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, ex.getMessage(), request);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
                log.warn("Data integrity violation: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.CONFLICT, "Database constraint violation occurred. Please check for duplicate entries.", request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
                log.warn("HTTP message not readable: {}", ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Malformed request or invalid data format.", request);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
                String message = "Invalid value for parameter '" + ex.getName() + "'";
                log.warn("Argument type mismatch for {}: {}", request.getRequestURI(), ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiError> handleMissingRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
                String message = "Missing required parameter: " + ex.getParameterName();
                log.warn("Missing request parameter for {}: {}", request.getRequestURI(), ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
                log.warn("No route found for {}: {}", request.getRequestURI(), ex.getMessage());
                return buildResponse(HmsErrorCode.BAD_REQUEST, HttpStatus.NOT_FOUND, "Requested endpoint was not found.", request);
        }

        @ExceptionHandler(NullPointerException.class)
        public ResponseEntity<ApiError> handleNullPointer(NullPointerException ex, HttpServletRequest request) {
                log.error("Null pointer prevented at {}", request.getRequestURI(), ex);
                return buildResponse(HmsErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Required data was missing while processing the request.", request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request) {
                log.error("Unexpected error occurred at {}", request.getRequestURI(), ex);
                return buildResponse(HmsErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null, request);
        }
}
