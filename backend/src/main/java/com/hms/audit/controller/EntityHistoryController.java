package com.hms.audit.controller;

import com.hms.audit.dto.EntityRevisionDTO;
import com.hms.audit.exception.AuditEntityTypeNotSupportedException;
import com.hms.audit.service.EntityHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit/history")
@RequiredArgsConstructor
public class EntityHistoryController {

    private final EntityHistoryService historyService;

    private static final Map<String, String> ENTITY_MAP = Map.of(
        "patient", "com.hms.patient.entity.Patient",
        "billing", "com.hms.billing.entity.Billing",
        "appointment", "com.hms.appointment.entity.Appointment",
        "prescription", "com.hms.prescription.entity.Prescription"
    );

    @GetMapping("/{entityType}/{id}")
    public List<? extends EntityRevisionDTO<?>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long id) {

        String entityClassName = ENTITY_MAP.get(entityType.toLowerCase());
        if (entityClassName == null) {
            throw new AuditEntityTypeNotSupportedException("Unknown entity type: " + entityType);
        }

        try {
            Class<?> entityClass = Class.forName(entityClassName);
            return historyService.getHistory(entityClass, id);
        } catch (ClassNotFoundException ex) {
            throw new AuditEntityTypeNotSupportedException("Unsupported entity type: " + entityType);
        }
    }
}
