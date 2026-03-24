package com.hms.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // SELECT * FROM audit_logs WHERE username = :username ORDER BY created_at DESC
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    // SELECT * FROM audit_logs WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY created_at DESC
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
