package com.hms.common.audit;

import com.hms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

/**
 * Persistent audit trail for important actions (create/update/delete, login, etc.).
 */
@Entity
@Table(name = "audit_log", indexes = {
        @jakarta.persistence.Index(name = "idx_audit_username", columnList = "username"),
        @jakarta.persistence.Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
        @jakarta.persistence.Index(name = "idx_audit_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(name = "entity_type", length = 64)
    private String entityType;

    @Column(name = "entity_id", length = 64)
    private String entityId;

    @Column(length = 1024)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
