package com.hms.billing.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import com.hms.security.idempotency.IdempotencyService;
import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.common.enums.PaymentStatus;
import com.hms.billing.service.BillingService;
import com.hms.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BillingController {

    private final BillingService billingService;
        private final IdempotencyService idempotencyService;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<ApiResponse<BillingResponseDTO>> createBilling(
            @Valid @RequestBody BillingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        billingService.createBilling(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<BillingResponseDTO>>> getAllBillings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Slice<BillingResponseDTO> slice = billingService.getBillingSlice(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100));

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<BillingResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }


    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Slice<BillingResponseDTO>>> getPagedBillings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getBillingSlice(page, size)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<BillingResponseDTO>>> getBillingSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query) {
        Slice<BillingResponseDTO> slice = billingService.getBillingSlice(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                query);

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<BillingResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<SliceResponse<BillingResponseDTO>>> getBillingsByPatientId(
            @PathVariable("patientId") Long patientId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Slice<BillingResponseDTO> slice = billingService.getBillingsByPatientIdPaged(patientId, page, size);
        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<BillingResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }


    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SliceResponse<BillingResponseDTO>>> getCurrentPatientBillings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        // Fix #10 - Enforce mandatory pagination 
        org.springframework.data.domain.Slice<BillingResponseDTO> slice = billingService.getCurrentPatientBillingsPaged(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 50));

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<BillingResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/preview-appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> previewBillingJson(
            @PathVariable("appointmentId") Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.calculatePreviewBilling(appointmentId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> getBillingById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.getBillingById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> updatePaymentStatus(
            @PathVariable("id") Long id, @RequestParam("status") PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.updatePaymentStatus(id, status)));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PatchMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> payCurrentPatientBill(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                billingService.payCurrentPatientBill(id), "Payment recorded"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping("/generate/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<BillingResponseDTO>> generateBilling(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        String currentUser = SecurityUtils.getCurrentUsername();
        String fingerprint = idempotencyService.computeFingerprint("appointmentId", appointmentId);

        BillingResponseDTO response = idempotencyService.execute(
                idempotencyKey,
                "billing-generate-appointment",
                currentUser,
                fingerprint,
                () -> billingService.generateBillingFromAppointment(appointmentId),
                BillingResponseDTO::getId,
                billingService::getBillingById);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBilling(
            @PathVariable("id") Long id) {
        billingService.deleteBilling(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
