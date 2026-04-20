package com.hms.audit.repository;

import com.hms.audit.entity.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Slice<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

        @Query("SELECT a FROM AuditLog a " +
            "WHERE LOWER(a.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.action) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.details) LIKE LOWER(CONCAT('%', :query, '%'))")
    Slice<AuditLog> searchAuditLogs(@Param("query") String query, Pageable pageable);
}
