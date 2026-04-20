package com.hms.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private Instant createdAt;
}
