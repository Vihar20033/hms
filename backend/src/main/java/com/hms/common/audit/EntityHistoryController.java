package com.hms.common.audit;

import com.hms.appointment.entity.Appointment;
import com.hms.billing.entity.Billing;
import com.hms.common.audit.dto.EntityRevisionDTO;
import com.hms.common.exception.BadRequestException;
import com.hms.patient.entity.Patient;
import com.hms.prescription.entity.Prescription;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit/history")
@RequiredArgsConstructor
public class EntityHistoryController {

    private final EntityHistoryService historyService;

    private static final Map<String, Class<?>> ENTITY_MAP = Map.of(
            "patient", Patient.class,
            "billing", Billing.class,
            "appointment", Appointment.class,
            "prescription", Prescription.class
    );

    @GetMapping("/{entityType}/{id}")
    public List<? extends EntityRevisionDTO<?>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable Long id) {
        
        Class<?> entityClass = ENTITY_MAP.get(entityType.toLowerCase());
        if (entityClass == null) {
            throw new BadRequestException("Unknown entity type: " + entityType);
        }

        return historyService.getHistory(entityClass, id);
    }
}
