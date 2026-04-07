package com.hms.common.audit;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger AUDIT_FILE = LoggerFactory.getLogger("AUDIT");

    private final AuditLogRepository repository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, String action, String entityType, String entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .username(username != null ? username : "system")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details != null && details.length() > 1024 ? details.substring(0, 1024) : details)
                .build();
        repository.save(entry);

        AUDIT_FILE.info("{} | {} | {} | {} | {}", entry.getUsername(), action, entityType, entityId, details != null ? details : "");
    }
}
