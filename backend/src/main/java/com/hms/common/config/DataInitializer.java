package com.hms.common.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting database column initialization...");

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
