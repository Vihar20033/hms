package com.hms.patient.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PatientController {

    private final PatientService service;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponseDTO>> create(
            @Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(dto), "Request successful", HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PHARMACIST')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PHARMACIST')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<PatientResponseDTO>>> getSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query) {
        
        org.springframework.data.domain.Slice<PatientResponseDTO> slice;
        String normalizedQuery = query == null ? "" : query.trim();
        if (!normalizedQuery.isEmpty()) {
            slice = service.getSearchableSlice(Math.max(page, 0), Math.min(Math.max(size, 1), 100), normalizedQuery);
        } else {
            slice = service.getSlice(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        }

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<PatientResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PHARMACIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponseDTO>> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponseDTO>> update(
            @PathVariable("id") Long id, @Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
