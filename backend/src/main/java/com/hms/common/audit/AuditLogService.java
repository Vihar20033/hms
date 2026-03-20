package com.hms.common.audit;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    /** Logger configured in logback to write to logs/audit.log */
    private static final Logger AUDIT_FILE = LoggerFactory.getLogger("AUDIT");

    private final AuditLogRepository repository;

    /**
     * Log an audit event (persisted to DB and appended to audit log file).
     * Runs async so it does not block the main request.
     */
    @Async
    @Transactional
    public void log(String username, String action, String entityType, String entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .username(username != null ? username : "system")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details != null && details.length() > 1024 ? details.substring(0, 1024) : details)
                .build();
        repository.save(entry);
        // Also write to audit log file (single line for grep/parsing)
        AUDIT_FILE.info("{} | {} | {} | {} | {}", entry.getUsername(), action, entityType, entityId, details != null ? details : "");
    }
}
