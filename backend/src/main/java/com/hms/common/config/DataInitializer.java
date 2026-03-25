package com.hms.common.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private final EntityManager entityManager;

    private static final List<String> ENTITY_NAMES = List.of(
            "User", "Patient", "Doctor", "DoctorSchedule", "Appointment", 
            "Billing", "LabReport", "LabTest", "Medicine", "Prescription", 
            "Vitals", "InventoryTransaction", "AuditLog"
    );

    @Override
    @Transactional
    public void run(String... args) {

        log.info("Starting database column initialization...");

        for (String entityName : ENTITY_NAMES) {
            try {
                int updatedCount = entityManager.createQuery("UPDATE " + entityName + " e SET e.version = 0 WHERE e.version IS NULL")
                        .executeUpdate();
                if (updatedCount > 0) {
                    log.info("Updated {} records for entity {} setting version to 0", updatedCount, entityName);
                }
            } catch (Exception e) {
                log.warn("Failed to update version for entity {}: {}", entityName, e.getMessage());
            }
        }

        try {
            int userCount = entityManager.createQuery(
                    "UPDATE User u SET u.tokenVersion = 0 WHERE u.tokenVersion IS NULL")
                    .executeUpdate();
            userCount += entityManager.createQuery(
                    "UPDATE User u SET u.enabled = true WHERE u.enabled IS NULL")
                    .executeUpdate();
            userCount += entityManager.createQuery(
                    "UPDATE User u SET u.passwordChangeRequired = false WHERE u.passwordChangeRequired IS NULL")
                    .executeUpdate();
            if (userCount > 0) {
                log.info("Initialized {} User-specific primitive fields", userCount);
            }
        } catch (Exception e) {
            log.warn("Failed to initialize user fields: {}", e.getMessage());
        }

        log.info("Database column initialization completed.");
    }
}
