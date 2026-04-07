package com.hms.common.audit;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<AuditLogResponse>>> getSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "25") int size) {
        PageRequest request = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<AuditLogResponse> slice = auditLogRepository.findAllByOrderByCreatedAtDesc(request)
                .map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<AuditLogResponse>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
